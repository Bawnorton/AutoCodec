package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;

public final class MemberReferenceNode extends ExpressionNode {
    private final JCTree.JCMemberReference memberReference;

    public MemberReferenceNode(JCTree.JCMemberReference memberReference) {
        this.memberReference = memberReference;
    }

    public JCTree.JCMemberReference getTree() {
        return memberReference;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private MemberReferenceTree.ReferenceMode mode;
        private Name name;
        private JCTree.JCExpression expression;
        private List<JCTree.JCExpression> typeArgs;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder mode(MemberReferenceTree.ReferenceMode mode) {
            this.mode = mode;
            return this;
        }

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            this.name = context.names().fromString(name);
            return this;
        }

        public Builder expression(JCTree.JCExpression expression) {
            this.expression = expression;
            return this;
        }

        public Builder expression(ExpressionNode expression) {
            return expression(expression.getTree());
        }

        public Builder expression(Name expression) {
            return expression(context.treeMaker().Ident(expression));
        }

        public Builder expression(String expression) {
            return expression(names().fromString(expression));
        }

        public Builder typeArg(JCTree.JCExpression typeArg) {
            if (typeArgs == null) typeArgs = List.nil();

            typeArgs = typeArgs.append(typeArg);
            return this;
        }

        public MemberReferenceNode build() {
            if (mode == null) throw new IllegalStateException("mode is required");
            if (name == null) throw new IllegalStateException("name is required");

            JCTree.JCMemberReference memberReference = context.treeMaker().Reference(mode, name, expression, typeArgs);
            return new MemberReferenceNode(memberReference);
        }
    }
}
