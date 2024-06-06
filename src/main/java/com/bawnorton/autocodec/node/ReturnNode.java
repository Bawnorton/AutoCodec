package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;

public final class ReturnNode extends StatementNode {
    private final JCTree.JCReturn returnNode;

    public ReturnNode(JCTree.JCReturn returnNode) {
        this.returnNode = returnNode;
    }

    public JCTree.JCReturn getTree() {
        return returnNode;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression expression;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder expression(JCTree.JCExpression expression) {
            this.expression = expression;
            return this;
        }

        public Builder expression(ExpressionNode expression) {
            return expression(expression.getTree());
        }

        public ReturnNode build() {
            JCTree.JCReturn returnNode = context.treeMaker().Return(expression);
            return new ReturnNode(returnNode);
        }
    }
}
