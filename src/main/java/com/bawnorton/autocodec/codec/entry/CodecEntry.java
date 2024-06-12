package com.bawnorton.autocodec.codec.entry;

import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

/**
 * A codec entry inside a group evaluation.
 * @param field the fieldInfo this entry is associated with
 * @param codecNode the expression to encode/decode the fieldInfo
 */
public record CodecEntry(FieldInfo field, ExpressionNode codecNode) {
}
