package com.bawnorton.autocodec.codec.clazz.factory.field;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.util.List;

public abstract class FieldHandler extends ContextHolder {
    public FieldHandler(ProcessingContext context) {
        super(context);
    }

    public abstract VariableDeclNode asParameter(VariableDeclNode field);

    public abstract List<StatementNode> createAssignmentStatements(VariableDeclNode field);
}
