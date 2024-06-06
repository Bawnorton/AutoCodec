package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;

public abstract class StatementNode extends TreeNode {
    public abstract JCTree.JCStatement getTree();
}
