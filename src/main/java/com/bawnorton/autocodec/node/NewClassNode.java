package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;

public final class NewClassNode extends ExpressionNode {
    private final JCTree.JCNewClass newClass;

    public NewClassNode(JCTree.JCNewClass newClass) {
        this.newClass = newClass;
    }

    public JCTree.JCNewClass getTree() {
        return newClass;
    }
}
