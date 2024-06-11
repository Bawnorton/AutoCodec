package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public final class BlockNode extends StatementNode {
    private final JCTree.JCBlock block;

    public BlockNode(JCTree.JCBlock block) {
        this.block = block;
    }

    public JCTree.JCBlock getTree() {
        return block;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private long flags;
        private List<JCTree.JCStatement> statements = List.nil();

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder flags(long flags) {
            this.flags = flags;
            return this;
        }

        public Builder statement(JCTree.JCStatement statement) {
            statements = statements.append(statement);
            return this;
        }

        public Builder statement(StatementNode statement) {
            return statement(statement.getTree());
        }

        public Builder statements(List<StatementNode> statements) {
            statements.forEach(this::statement);
            return this;
        }

        public BlockNode build() {
            JCTree.JCBlock block = treeMaker().Block(flags, statements);
            return new BlockNode(block);
        }
    }
}
