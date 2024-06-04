package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;

public final class IdentNode extends ExpressionNode {
    private final JCTree.JCIdent ident;

    public IdentNode(JCTree.JCIdent ident) {
        this.ident = ident;
    }

    public JCTree.JCIdent getTree() {
        return ident;
    }

    public static IdentNode of(ProcessingContext context, String name) {
        return builder(context).name(name).build();
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private String name;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public IdentNode build() {
            JCTree.JCIdent ident = treeMaker().Ident(names().fromString(name));
            return new IdentNode(ident);
        }
    }
}
