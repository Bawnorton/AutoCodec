package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public final class MethodInvocationNode {
    private final JCTree.JCMethodInvocation methodInvocation;

    public MethodInvocationNode(JCTree.JCMethodInvocation methodInvocation) {
        this.methodInvocation = methodInvocation;
    }

    public JCTree.JCMethodInvocation getMethodInvocation() {
        return methodInvocation;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final ProcessingContext context;
        private JCTree.JCExpression methodSelect;
        private List<JCTree.JCExpression> arguments;

        public Builder(ProcessingContext context) {
            this.context = context;
        }

        public Builder methodSelect(JCTree.JCExpression methodSelect) {
            this.methodSelect = methodSelect;
            return this;
        }

        public Builder arguments(List<JCTree.JCExpression> arguments) {
            this.arguments = arguments;
            return this;
        }

        public MethodInvocationNode build() {
            JCTree.JCMethodInvocation methodInvocation = context.treeMaker().App(methodSelect, arguments);
            return new MethodInvocationNode(methodInvocation);
        }
    }
}
