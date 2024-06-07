package com.bawnorton.autocodec.node.creator;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Flags;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class RecordConstructorCreator extends ConstructorCreator {
    private final MethodDeclNode mainCtor;
    private final Map<VariableDeclNode, List<AnnotationNode>> paramAnnotations;

    public RecordConstructorCreator(ClassDeclNode classDeclNode) {
        super(classDeclNode);
        this.paramAnnotations = new HashMap<>();
        this.mainCtor = findMainRecordCtor();
    }

    @Override
    protected Supplier<Boolean> validate() {
        return classDeclNode::isRecord;
    }

    private MethodDeclNode findMainRecordCtor() {
        List<MethodDeclNode> constructors = classDeclNode.getConstructors();
        for (MethodDeclNode constructor : constructors) {
            List<VariableDeclNode> fields = classDeclNode.getFields();
            List<VariableDeclNode> parameters = constructor.getParameters();
            if (fields.size() != parameters.size()) continue;

            for (int i = 0; i < fields.size(); i++) {
                VariableDeclNode field = fields.get(i);
                VariableDeclNode parameter = parameters.get(i);

                paramAnnotations.put(parameter, field.getAnnotations());
                if (!field.sameNameAndType(parameter)) {
                    throw new AssertionError("Field and parameter types do not match: Found " + field + " but expected " + parameter);
                }
            }
            return constructor;
        }
        throw new AssertionError("Record missing main constructor.");
    }

    @Override
    public void createCtorForFields(ProcessingContext context, List<IncludedField> includedFields) {
        List<VariableDeclNode> parameters = new ArrayList<>();
        for (IncludedField includedField : includedFields) {
            VariableDeclNode field = includedField.fieldNode();
            VariableDeclNode parameter = VariableDeclNode.builder(context)
                    .modifiers(Flags.PARAMETER)
                    .name(field.getName())
                    .owner(classDeclNode)
                    .type(field.getType().tsym.name)
                    .build();
            parameters.add(parameter);
        }

        List<ExpressionNode> thisCallArguments = new ArrayList<>();
        for (VariableDeclNode parameter : mainCtor.getParameters()) {
            List<AnnotationNode> annotations = paramAnnotations.get(parameter);
            boolean ignore = annotations.stream().anyMatch(annotation -> annotation.isOfType(Ignore.class));
            if (ignore) {
                thisCallArguments.add(LiteralNode.nullLiteral(context));
            } else {
                thisCallArguments.add(IdentNode.of(context, parameter.getName()));
            }
        }

        MethodInvocationNode thisCallInvocationNode = MethodInvocationNode.builder(context)
                .methodSelect("this")
                .arguments(thisCallArguments)
                .build();

        BlockNode newCtorBody = BlockNode.builder(context)
                .statement(thisCallInvocationNode.toStatement(context.treeMaker()))
                .build();

        classDeclNode.addMethod(MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(context.names().init)
                .params(parameters)
                .body(newCtorBody)
                .build());
    }
}
