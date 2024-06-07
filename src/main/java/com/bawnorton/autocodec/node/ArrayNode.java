package com.bawnorton.autocodec.node;

import com.sun.tools.javac.tree.JCTree;

public final class ArrayNode extends ExpressionNode {
    private final JCTree.JCNewArray newArray;

    public ArrayNode(JCTree.JCNewArray newArray) {
        this.newArray = newArray;
    }

    public JCTree.JCNewArray getTree() {
        return newArray;
    }
}
