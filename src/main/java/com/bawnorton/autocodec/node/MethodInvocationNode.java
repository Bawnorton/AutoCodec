package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public final class MethodInvocationNode extends ExpressionNode {
    private final JCTree.JCMethodInvocation methodInvocation;

    public MethodInvocationNode(JCTree.JCMethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public JCTree.JCMethodInvocation getTree() {
        return methodInvocation;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression methodSelect;
        private List<JCTree.JCExpression> arguments = List.nil();
        private List<JCTree.JCExpression> typeArguments = List.nil();

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder methodSelect(JCTree.JCExpression methodSelect) {
            this.methodSelect = methodSelect;
            return this;
        }

        public Builder methodSelect(ExpressionNode methodSelect) {
            return methodSelect(methodSelect.getTree());
        }

        public Builder methodSelect(Name name) {
            return methodSelect(context.treeMaker().Ident(name));
        }

        public Builder methodSelect(String name) {
            return methodSelect(context.names().fromString(name));
        }

        public Builder argument(JCTree.JCExpression argument) {
            arguments = arguments.append(argument);
            return this;
        }

        public Builder argument(Name name) {
            return argument(context.treeMaker().Ident(name));
        }

        public Builder argument(String name) {
            return argument(context.names().fromString(name));
        }

        public Builder argument(ExpressionNode argument) {
            return argument(argument.getTree());
        }

        public Builder arguments(Iterable<? extends ExpressionNode> arguments) {
            List<JCTree.JCExpression> argumentList = List.nil();
            for (ExpressionNode argument : arguments) {
                argumentList = argumentList.append(argument.getTree());
            }
            this.arguments = argumentList;
            return this;
        }

        public Builder typeArgument(JCTree.JCIdent typeArgument) {
            typeArguments = typeArguments.append(typeArgument);
            return this;
        }

        public Builder typeArgument(Name typeArgument) {
            return typeArgument(context.treeMaker().Ident(typeArgument));
        }

        public Builder typeArgument(String typeArgument) {
            return typeArgument(context.names().fromString(typeArgument));
        }

        public Builder typeArgument(ClassDeclNode typeArgument) {
            return typeArgument(typeArgument.getName());
        }

        public Builder typeArgument(Type.ClassType typeArgument) {
            return typeArgument(typeArgument.tsym.name);
        }

        public MethodInvocationNode build() {
            JCTree.JCMethodInvocation methodInvocation = context.treeMaker().Apply(typeArguments, methodSelect, arguments);
            return new MethodInvocationNode(methodInvocation);
        }
    }
}
