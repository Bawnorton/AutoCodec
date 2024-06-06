package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;

public final class LiteralNode extends ExpressionNode {
    private final JCTree.JCLiteral literal;

    public LiteralNode(JCTree.JCLiteral literal) {
        this.literal = literal;
    }

    public JCTree.JCLiteral getTree() {
        return literal;
    }

    public static LiteralNode nullLiteral(ProcessingContext context) {
        return new LiteralNode(context.treeMaker().Literal(TypeTag.BOT, null));
    }

    public static LiteralNode defaultLiteral(ProcessingContext context, Type type) {
        TypeTag typeTag = type.getTag();
        Object value = switch (typeTag) {
            case BOOLEAN -> false;
            case BYTE -> (byte) 0;
            case SHORT -> (short) 0;
            case INT -> 0;
            case LONG -> 0L;
            case CHAR -> '\u0000';
            case FLOAT -> 0.0f;
            case DOUBLE -> 0.0;
            default -> null;
        };
        if (value == null) return nullLiteral(context);

        return new LiteralNode(context.treeMaker().Literal(typeTag, value));
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder {
        private final ProcessingContext context;
        private Object value;

        private Builder(ProcessingContext context) {
            this.context = context;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public LiteralNode build() {
            JCTree.JCLiteral literal = context.treeMaker().Literal(value);
            return new LiteralNode(literal);
        }
    }
}
