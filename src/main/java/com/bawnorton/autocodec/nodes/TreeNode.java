package com.bawnorton.autocodec.nodes;

import com.sun.tools.javac.tree.JCTree;

public abstract class TreeNode {
    public abstract JCTree getTree();

    @Override
    public String toString() {
        return getTree().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeNode) {
            return getTree().equals(((TreeNode) obj).getTree());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getTree().hashCode();
    }
}
