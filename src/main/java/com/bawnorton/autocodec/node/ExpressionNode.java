package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

public abstract class ExpressionNode extends TreeNode {
    public abstract JCTree.JCExpression getTree();

    public ExpressionStatementNode toStatement(TreeMaker maker) {
        return new ExpressionStatementNode(maker.Exec(getTree()));
    }
}
