package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;

public abstract class ExpressionNode extends TreeNode {
    public abstract JCTree.JCExpression getTree();

    public JCTree.JCExpressionStatement toStatement(TreeMaker maker) {
        return maker.Exec(getTree());
    }

    public JCTree.JCReturn toReturn(TreeMaker maker) {
        return maker.Return(getTree());
    }
}
