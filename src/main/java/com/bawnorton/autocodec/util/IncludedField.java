package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.OptionalEntry;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public record IncludedField(VariableDeclNode fieldNode, AnnotationNode optional) {
    public boolean isOptional() {
        return optional != null;
    }

    public boolean hasOptionalValue() {
        return isOptional() && optional.value(LiteralNode.class) != null;
    }

    /**
     * The value passed to the {@link OptionalEntry} annotation.
     * <br>This can be a method reference or java expression.
     * <br>For example, {@code @Optional("defaultResolver")} or {@code @Optional("new ArrayList<>()")}
     */
    public String getOptionalValue() {
        return optional.value(LiteralNode.class).getValue(String.class);
    }
}
