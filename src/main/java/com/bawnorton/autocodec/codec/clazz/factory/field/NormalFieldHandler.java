package com.bawnorton.autocodec.codec.clazz.factory.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AssignNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.IdentNode;
import com.bawnorton.autocodec.node.StatementNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.List;

public final class NormalFieldHandler extends FieldHandler {
    public NormalFieldHandler(ProcessingContext context) {
        super(context);
    }

    @Override
    public VariableDeclNode asParameter(VariableDeclNode field) {
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getName())
                .type(field.getSimpleTypeName())
                .genericParams(field.getGenericTypes())
                .noSym()
                .build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(VariableDeclNode field) {
        // this.field
        FieldAccessNode assignmentNode = FieldAccessNode.builder(context)
                .selected(IdentNode.of(context, "this"))
                .name(field.getName())
                .build();

        // this.field = field
        return List.of(AssignNode.builder(context)
                .lhs(assignmentNode)
                .rhs(field.getName())
                .build()
                .toStatement(treeMaker()));
    }
}
