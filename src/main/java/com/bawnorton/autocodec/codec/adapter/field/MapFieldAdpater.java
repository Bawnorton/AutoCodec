package com.bawnorton.autocodec.codec.adapter.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.helper.TypeHelper;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class MapFieldAdpater extends FieldAdpater {
    public MapFieldAdpater(ProcessingContext context) {
        super(context);
    }

    @Override
    public Type getParameterType(Type fieldType) {
        return TypeHelper.findType(context, fieldType, java.util.Map.class);
    }

    @Override
    public VariableDeclNode getParameter(FieldInfo field) {
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getNameString())
                .type("Map")
                .genericParams(field.getGenericTypes())
                .build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(FieldInfo field) {
        return List.nil();
    }
}
