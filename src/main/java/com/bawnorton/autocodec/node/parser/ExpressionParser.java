package com.bawnorton.autocodec.node.parser;

import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;
import javax.tools.Diagnostic;

public class ExpressionParser {
    public static ExpressionNode parseExpression(ProcessingContext context, String expressionStr) {
        JavacParser parser = context.parserFactory().newParser(expressionStr, false, false, false);
        JCTree.JCExpression expression = parser.parseExpression();
        if (parser.token().kind != Tokens.TokenKind.EOF) {
            context.messager().printMessage(Diagnostic.Kind.ERROR, "Unexpected token: " + parser.token());
        }
        return asExpressionNode(expression);
    }

    public static ExpressionNode asExpressionNode(JCTree.JCExpression expression) {
        if (expression instanceof JCTree.JCAssign assign) {
            return new AssignNode(assign);
        } else if (expression instanceof JCTree.JCMethodInvocation methodInvocation) {
            return new MethodInvocationNode(methodInvocation);
        } else if (expression instanceof JCTree.JCFieldAccess fieldAccess) {
            return new FieldAccessNode(fieldAccess);
        } else if (expression instanceof JCTree.JCLiteral literal) {
            return new LiteralNode(literal);
        } else if (expression instanceof JCTree.JCNewArray newArray) {
            return new ArrayNode(newArray);
        } else if (expression instanceof JCTree.JCAnnotation annotation) {
            return new AnnotationNode(annotation);
        } else if (expression instanceof JCTree.JCIdent ident) {
            return new IdentNode(ident);
        } else if (expression instanceof JCTree.JCLambda lambda) {
            return new LambdaNode(lambda);
        } else if (expression instanceof JCTree.JCMemberReference memberReference) {
            return new MemberReferenceNode(memberReference);
        } else if (expression instanceof JCTree.JCNewClass newClass) {
            return new NewClassNode(newClass);
        }
        throw new IllegalArgumentException("Unsupported expression type: " + expression.getClass().getName());
    }
}
