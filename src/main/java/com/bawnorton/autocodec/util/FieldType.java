package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.VariableDeclNode;

public enum FieldType {
    NORMAL, LIST, MAP;

    public static FieldType ofField(ProcessingContext context, VariableDeclNode field) {
        if(TypeUtils.isOf(context, field.getType(), java.util.List.class)) {
            return FieldType.LIST;
        } else if (TypeUtils.isOf(context, field.getType(), java.util.Map.class)) {
            return FieldType.MAP;
        }
        return FieldType.NORMAL;
    }
}