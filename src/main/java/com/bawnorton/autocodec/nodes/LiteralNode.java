package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;

public final class LiteralNode extends ExpressionNode {
    private final JCTree.JCLiteral literal;

    public LiteralNode(JCTree.JCLiteral literal) {
        this.literal = literal;
    }

    public JCTree.JCLiteral getTree() {
        return literal;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final ProcessingContext context;
        private Object value;

        private Builder(ProcessingContext context) {
            this.context = context;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public LiteralNode build() {
            JCTree.JCLiteral literal = context.treeMaker().Literal(value);
            return new LiteralNode(literal);
        }
    }
}
