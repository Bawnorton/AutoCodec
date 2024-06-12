package com.bawnorton.autocodec.codec.adapter.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.AssignNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.IdentNode;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class NormalFieldAdpater extends FieldAdpater {
    public NormalFieldAdpater(ProcessingContext context) {
        super(context);
    }

    @Override
    public Type getParameterType(Type fieldType) {
        return fieldType;
    }

    @Override
    public VariableDeclNode getParameter(FieldInfo field) {
        VariableDeclNode.Builder parameterBuilder = VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getName());
        if(field.isPrimitive()) {
            parameterBuilder.primitiveType(field.getType().getTag());
        } else {
            parameterBuilder.type(field.getSimpleTypeName()).genericParams(field.getGenericTypes());
        }
        return parameterBuilder.build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(FieldInfo field) {
        // this.fieldInfo
        FieldAccessNode assignmentNode = FieldAccessNode.builder(context)
                .selected(IdentNode.of(context, "this"))
                .name(field.getName())
                .build();

        // this.fieldInfo = fieldInfo
        return List.of(AssignNode.builder(context)
                .lhs(assignmentNode)
                .rhs(field.getName())
                .build()
                .toStatement(treeMaker()));
    }
}
