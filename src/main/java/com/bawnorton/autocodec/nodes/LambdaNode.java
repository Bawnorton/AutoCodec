package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

public final class LambdaNode extends ExpressionNode {
    private final JCTree.JCLambda lambda;

    public LambdaNode(JCTree.JCLambda lambda) {
        this.lambda = lambda;
    }

    public JCTree.JCLambda getTree() {
        return lambda;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private List<JCTree.JCVariableDecl> params = List.nil();
        private JCTree.JCMethodInvocation body;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder param(JCTree.JCVariableDecl param) {
            params = params.append(param);
            return this;
        }

        public Builder param(VariableDeclNode param) {
            return param(param.getTree());
        }

        public Builder body(JCTree.JCMethodInvocation body) {
            this.body = body;
            return this;
        }

        public Builder body(MethodInvocationNode body) {
            return body(body.getTree());
        }

        public LambdaNode build() {
            JCTree.JCLambda lambda = context.treeMaker().Lambda(params, body);
            return new LambdaNode(lambda);
        }
    }
}
