package com.bawnorton.autocodec.codec;

import com.bawnorton.autocodec.nodes.MethodInvocationNode;
import com.bawnorton.autocodec.nodes.VariableDeclNode;
import com.sun.tools.javac.code.Type;

public record CodecReference(VariableDeclNode fieldNode, MethodInvocationNode codecNode) {
    public Type.ClassType getType() {
        return (Type.ClassType) fieldNode.getType();
    }
}
