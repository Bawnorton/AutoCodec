package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.node.VariableDeclNode;

public record IncludedField(VariableDeclNode variableDeclNode, boolean optional) {
}
