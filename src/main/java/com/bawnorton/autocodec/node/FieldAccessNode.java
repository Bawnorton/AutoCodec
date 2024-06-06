package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

public final class FieldAccessNode extends ExpressionNode {
    private final JCTree.JCFieldAccess fieldAccess;

    public FieldAccessNode(JCTree.JCFieldAccess fieldAccess) {
        this.fieldAccess = fieldAccess;
    }

    public JCTree.JCFieldAccess getTree() {
        return fieldAccess;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression selected;
        private Name name;

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder selected(JCTree.JCExpression selected) {
            this.selected = selected;
            return this;
        }

        public Builder selected(ExpressionNode selected) {
            return selected(selected.getTree());
        }

        public Builder selected(String selected) {
            return selected(context.treeMaker().Ident(context.names().fromString(selected)));
        }

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            this.name = context.names().fromString(name);
            return this;
        }

        public FieldAccessNode build() {
            JCTree.JCFieldAccess fieldAccess = context.treeMaker().Select(selected, name);
            return new FieldAccessNode(fieldAccess);
        }
    }
}
