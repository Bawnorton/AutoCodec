package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public final class NewClassNode extends ExpressionNode {
    private final JCTree.JCNewClass newClass;

    public NewClassNode(JCTree.JCNewClass newClass) {
        this.newClass = newClass;
    }

    public JCTree.JCNewClass getTree() {
        return newClass;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression encl;
        private List<JCTree.JCExpression> typeargs = List.nil();
        private JCTree.JCExpression clazz;
        private List<JCTree.JCExpression> args = List.nil();
        private JCTree.JCClassDecl def;

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder encl(JCTree.JCExpression encl) {
            this.encl = encl;
            return this;
        }

        public Builder encl(ExpressionNode encl) {
            return encl(encl.getTree());
        }

        public Builder typearg(JCTree.JCExpression typearg) {
            typeargs = typeargs.append(typearg);
            return this;
        }

        public Builder typearg(ExpressionNode typearg) {
            return typearg(typearg.getTree());
        }

        public Builder clazz(JCTree.JCExpression clazz) {
            this.clazz = clazz;
            return this;
        }

        public Builder clazz(ExpressionNode clazz) {
            return clazz(clazz.getTree());
        }

        public Builder arg(JCTree.JCExpression arg) {
            args = args.append(arg);
            return this;
        }

        public Builder arg(ExpressionNode arg) {
            return arg(arg.getTree());
        }

        public Builder def(JCTree.JCClassDecl def) {
            this.def = def;
            return this;
        }

        public Builder def(ClassDeclNode def) {
            return def(def.getTree());
        }

        public NewClassNode build() {
            return new NewClassNode(treeMaker().NewClass(encl, typeargs, clazz, args, def));
        }
    }
}
