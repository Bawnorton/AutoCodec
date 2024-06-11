package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;

public final class ExpressionStatementNode extends StatementNode {
    private final JCTree.JCExpressionStatement expressionStatement;

    public ExpressionStatementNode(JCTree.JCExpressionStatement expressionStatement) {
        this.expressionStatement = expressionStatement;
    }

    public JCTree.JCExpressionStatement getTree() {
        return expressionStatement;
    }
}
