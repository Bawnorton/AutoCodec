package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Name;

public final class IdentNode extends ExpressionNode {
    private final JCTree.JCIdent ident;

    public IdentNode(JCTree.JCIdent ident) {
        this.ident = ident;
    }

    public JCTree.JCIdent getTree() {
        return ident;
    }

    public Name getName() {
        return ident.name;
    }

    public static IdentNode of(ProcessingContext context, Name name) {
        return builder(context).name(name).build();
    }

    public static IdentNode of(ProcessingContext context, String name) {
        return builder(context).name(name).build();
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private Name name;

        private Builder(ProcessingContext context) {
            super(context);
        }

        private Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            this.name = names().fromString(name);
            return this;
        }

        public IdentNode build() {
            JCTree.JCIdent ident = treeMaker().Ident(name);
            return new IdentNode(ident);
        }
    }
}
