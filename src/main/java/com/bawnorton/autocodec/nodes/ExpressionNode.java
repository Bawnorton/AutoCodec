package com.bawnorton.autocodec.nodes;

import com.sun.tools.javac.tree.JCTree;

public abstract class ExpressionNode extends TreeNode {
    public abstract JCTree.JCExpression getTree();
}
