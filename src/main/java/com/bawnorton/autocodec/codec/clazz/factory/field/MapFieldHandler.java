package com.bawnorton.autocodec.codec.clazz.factory.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.List;

public final class MapFieldHandler extends FieldHandler {
    public MapFieldHandler(ProcessingContext context) {
        super(context);
    }

    @Override
    public VariableDeclNode asParameter(VariableDeclNode field) {
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getName())
                .type("Map")
                .genericParams(field.getGenericTypes())
                .noSym()
                .build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(VariableDeclNode field) {
        return List.nil();
    }
}
