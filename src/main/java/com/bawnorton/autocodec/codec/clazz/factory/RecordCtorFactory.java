package com.bawnorton.autocodec.codec.clazz.factory;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class RecordCtorFactory extends CtorFactory {
    private final MethodDeclNode mainCtor;
    private final Map<VariableDeclNode, List<AnnotationNode>> paramAnnotations;

    public RecordCtorFactory(ProcessingContext context, ClassDeclNode classDeclNode) {
        super(context, classDeclNode);
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
    public MethodDeclNode createCtorForFields(List<CodecEntryField> codecEntryFields) {
        List<VariableDeclNode> parameters = List.nil();
        for (CodecEntryField codecEntryField : codecEntryFields) {
            VariableDeclNode field = codecEntryField.field();
            VariableDeclNode parameter = VariableDeclNode.builder(context)
                    .modifiers(Flags.PARAMETER)
                    .name(field.getName())
                    .type(field.getType().tsym.name)
                    .build();
            parameters = parameters.append(parameter);
        }

        List<ExpressionNode> thisCallArguments = List.nil();
        for (VariableDeclNode parameter : mainCtor.getParameters()) {
            List<AnnotationNode> annotations = paramAnnotations.get(parameter);
            boolean ignore = annotations.stream().anyMatch(annotation -> annotation.isOfType(Ignore.class));
            if (ignore) {
                thisCallArguments = thisCallArguments.append(LiteralNode.nullLiteral(context));
            } else {
                thisCallArguments = thisCallArguments.append(IdentNode.of(context, parameter.getName()));
            }
        }

        MethodInvocationNode thisCallInvocationNode = MethodInvocationNode.builder(context)
                .methodSelect("this")
                .arguments(thisCallArguments)
                .build();

        BlockNode newCtorBody = BlockNode.builder(context)
                .statement(thisCallInvocationNode.toStatement(context.treeMaker()))
                .build();

        return MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(context.names().init)
                .params(parameters)
                .body(newCtorBody)
                .build();
    }
}
