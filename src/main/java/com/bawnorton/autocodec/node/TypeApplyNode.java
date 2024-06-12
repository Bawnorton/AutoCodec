package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public final class TypeApplyNode extends ExpressionNode {
    private final JCTree.JCTypeApply typeApply;

    public TypeApplyNode(JCTree.JCTypeApply typeApply) {
        this.typeApply = typeApply;
    }

    public JCTree.JCTypeApply getTree() {
        return typeApply;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private List<JCTree.JCExpression> typeArgs = List.nil();
        private JCTree.JCExpression clazz;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder clazz(JCTree.JCExpression clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder clazz(ExpressionNode clazz) {
            return clazz(clazz.getTree());
        }

        public Builder clazz(Name clazz) {
            return clazz(context.treeMaker().Ident(clazz));
        }

        public Builder clazz(String clazz) {
            return clazz(names().fromString(clazz));
        }

        public Builder typeArg(JCTree.JCExpression typeArg) {
            typeArgs = typeArgs.append(typeArg);
            return this;
        }

        public Builder typeArg(ExpressionNode typeArg) {
            return typeArg(typeArg.getTree());
        }

        public Builder typeArg(Name typeArg) {
            return typeArg(context.treeMaker().Ident(typeArg));
        }

        public Builder typeArg(String typeArg) {
            return typeArg(context.names().fromString(typeArg));
        }

        public Builder typeArgs(List<String> typeArgs) {
            typeArgs.forEach(this::typeArg);
            return this;
        }

        public TypeApplyNode build() {
            return new TypeApplyNode(context.treeMaker().TypeApply(clazz, typeArgs));
        }
    }
}
