package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.nodes.VariableDeclNode;

public record IncludedField(VariableDeclNode variableDeclNode, boolean optional) {
}
