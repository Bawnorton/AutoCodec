package com.bawnorton.autocodec.codec.entry;

import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

/**
 * A codec entry inside a group evaluation.
 * @param fieldNode the field this entry is associated with
 * @param codecNode the expression to encode/decode the field
 */
public record CodecEntry(VariableDeclNode fieldNode, ExpressionNode codecNode) {
}
