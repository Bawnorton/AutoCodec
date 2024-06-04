package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public final class BlockNode extends TreeNode {
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

        public Builder statements(List<JCTree.JCStatement> statements) {
            this.statements = statements;
            return this;
        }

        public BlockNode build() {
            JCTree.JCBlock block = treeMaker().Block(flags, statements);
            return new BlockNode(block);
        }
    }
}
