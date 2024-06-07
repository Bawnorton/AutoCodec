package com.bawnorton.autocodec.node.parser;

import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;

public class ExpressionParser {
    public static ExpressionNode parseExpression(ProcessingContext context, String expressionStr) {
        JavacParser parser = context.parserFactory().newParser(expressionStr, false, false, false);
        JCTree.JCExpression expression = parser.parseExpression();
        if (parser.token().kind != Tokens.TokenKind.EOF) {
            context.messager().printMessage(javax.tools.Diagnostic.Kind.ERROR, "Unexpected token: " + parser.token());
        }
        return ExpressionNode.asExpressionNode(expression);
    }
}
