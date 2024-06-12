package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

public final class PrimitiveTypeTreeNode extends ExpressionNode {
    private final JCTree.JCPrimitiveTypeTree primitiveTypeTree;

    public PrimitiveTypeTreeNode(JCTree.JCPrimitiveTypeTree primitiveTypeTree) {
        this.primitiveTypeTree = primitiveTypeTree;
    }

    @Override
    public JCTree.JCPrimitiveTypeTree getTree() {
        return primitiveTypeTree;
    }

    public static PrimitiveTypeTreeNode of(ProcessingContext context, TypeTag typeTag) {
        return builder(context).typeTag(typeTag).build();
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private TypeTag typeTag;

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder typeTag(TypeTag typeTag) {
            this.typeTag = typeTag;
            return this;
        }

        public PrimitiveTypeTreeNode build() {
            return new PrimitiveTypeTreeNode(treeMaker().TypeIdent(typeTag));
        }
    }
}
