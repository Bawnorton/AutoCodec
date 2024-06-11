package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

public final class AssignNode extends ExpressionNode {
    private final JCTree.JCAssign assign;

    public AssignNode(JCTree.JCAssign assign) {
        this.assign = assign;
    }

    public JCTree.JCAssign getTree() {
        return assign;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression lhs;
        private JCTree.JCExpression rhs;

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder lhs(JCTree.JCExpression lhs) {
            this.lhs = lhs;
            return this;
        }

        public Builder lhs(ExpressionNode lhs) {
            return lhs(lhs.getTree());
        }

        public Builder lhs(Name name) {
            return lhs(treeMaker().Ident(name));
        }

        public Builder lhs(String name) {
            return lhs(names().fromString(name));
        }

        public Builder rhs(JCTree.JCExpression rhs) {
            this.rhs = rhs;
            return this;
        }

        public Builder rhs(ExpressionNode rhs) {
            return rhs(rhs.getTree());
        }

        public Builder rhs(Name name) {
            return rhs(treeMaker().Ident(name));
        }

        public Builder rhs(String name) {
            return rhs(names().fromString(name));
        }

        public AssignNode build() {
            if (lhs == null || rhs == null) {
                throw new IllegalStateException("lhs and rhs must be set.");
            }
            return new AssignNode(treeMaker().Assign(lhs, rhs));
        }
    }
}
