package com.bawnorton.autocodec.codec;

import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.util.ProcessingContext;

public record CodecReference(ProcessingContext context, VariableDeclNode fieldNode, ExpressionNode codecNode) {
}
