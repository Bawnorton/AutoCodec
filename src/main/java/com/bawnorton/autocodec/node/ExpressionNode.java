package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

public abstract class ExpressionNode extends TreeNode {
    public abstract JCTree.JCExpression getTree();

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

    public JCTree.JCExpressionStatement toStatement(TreeMaker maker) {
        return maker.Exec(getTree());
    }

    public JCTree.JCReturn toReturn(TreeMaker maker) {
        return maker.Return(getTree());
    }
}
