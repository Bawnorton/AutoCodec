package com.bawnorton.autocodec.codec.clazz.factory.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.GenericHelper;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.util.TypeUtils;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class ListFieldHandler extends FieldHandler {
    public ListFieldHandler(ProcessingContext context) {
        super(context);
    }

    @Override
    public VariableDeclNode asParameter(VariableDeclNode field) {
        Type generic = GenericHelper.getFirstGenericOfClassInParentsOrThrow(context, field.getType(), java.util.List.class);
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getName())
                .type("List")
                .genericParam(generic)
                .noSym()
                .build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(VariableDeclNode field) {
        // this.field
        FieldAccessNode fieldReference = FieldAccessNode.builder(context)
                .selected(IdentNode.of(context, "this"))
                .name(field.getName())
                .build();

        // field is "List" exactly
        Type fieldType = field.getType();
        if(TypeUtils.is(fieldType, java.util.List.class)) {
            return List.of(AssignNode.builder(context)
                    .lhs(fieldReference)
                    .rhs(field.getName())
                    .build()
                    .toStatement(treeMaker()));
        } else {
            if(field.getType().isInterface()) {
                // TODO
                return List.nil();
            }

            return createAssignmentForListChild(field, fieldReference);
        }
    }

    private List<StatementNode> createAssignmentForListChild(VariableDeclNode field, FieldAccessNode fieldReference) {
        List<StatementNode> statements = List.nil();

        List<Type> generics = field.getGenericTypes();
        ExpressionNode newFieldTypeClazz;
        if(generics.isEmpty()) {
            newFieldTypeClazz = IdentNode.of(context, field.getSimpleTypeName());
        } else {
            newFieldTypeClazz = TypeApplyNode.builder(context)
                    .clazz(field.getSimpleTypeName())
                    .build();

        }
        NewClassNode newFieldType = NewClassNode.builder(context)
                .clazz(newFieldTypeClazz)
                .build();

        AssignNode fieldInit = AssignNode.builder(context)
                .lhs(fieldReference)
                .rhs(newFieldType)
                .build();

        statements = statements.append(fieldInit.toStatement(treeMaker()));

        FieldAccessNode addAllReference = FieldAccessNode.builder(context)
                .selected(fieldReference)
                .name("addAll")
                .build();

        MethodInvocationNode addAllInvocation = MethodInvocationNode.builder(context)
                .methodSelect(addAllReference)
                .argument(field.getName())
                .build();

        return statements.append(addAllInvocation.toStatement(treeMaker()));
    }
}
