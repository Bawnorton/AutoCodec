package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
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
        private List<JCTree.JCExpression> arguments;

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

        public Builder argument(JCTree.JCExpression argument) {
            if (arguments == null) {
                arguments = List.of(argument);
            } else {
                arguments = arguments.append(argument);
            }
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

        public MethodInvocationNode build() {
            JCTree.JCMethodInvocation methodInvocation = context.treeMaker().Apply(null, methodSelect, arguments);
            return new MethodInvocationNode(methodInvocation);
        }
    }
}
