package com.bawnorton.autocodec.codec.entry;

import com.bawnorton.autocodec.OptionalEntry;
import com.bawnorton.autocodec.codec.adapter.entry.EntryAdapter;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.AnnotationInfo;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.sun.tools.javac.code.Type;

/**
 * A fieldInfo in a {@link ClassDeclNode} that should be included in a codec definition.
 */
public final class CodecEntryField extends ContextHolder {
    private final FieldInfo fieldInfo;
    private final AnnotationInfo optional;

    public CodecEntryField(ProcessingContext context, FieldInfo fieldInfo) {
        super(context);
        this.fieldInfo = fieldInfo;
        this.optional = fieldInfo.getAnnotation(OptionalEntry.class);

        if(fieldInfo.getType().tsym.name.equals(names().any)) {
            throw new IllegalStateException("Cannot determine type for field \"" + fieldInfo.getNameString() + "\", ensure the type is imported");
        }
    }

    public boolean isOptional() {
        return optional != null;
    }

    public FieldInfo fieldInfo() {
        return fieldInfo;
    }

    public boolean fromParent() {
        return fieldInfo.fromParent();
    }

    public Type getParameterType() {
        return fieldAdapter(fieldInfo.getType()).getParameterType(fieldInfo.getType());
    }

    public EntryAdapter getAdapter() {
        if (isOptional()) {
            return optionalEntryAdapter(fieldInfo.getType(), optional);
        } else {
            return requiredEntryAdapter(fieldInfo.getType());
        }
    }
}
