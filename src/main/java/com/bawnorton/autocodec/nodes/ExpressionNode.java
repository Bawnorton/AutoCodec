package com.bawnorton.autocodec.nodes;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

public abstract class ExpressionNode extends TreeNode {
    public abstract JCTree.JCExpression getTree();

    public JCTree.JCExpressionStatement toStatement(TreeMaker maker) {
        return maker.Exec(getTree());
    }
}
