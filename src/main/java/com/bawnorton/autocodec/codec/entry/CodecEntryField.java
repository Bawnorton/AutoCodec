package com.bawnorton.autocodec.codec.entry;

import com.bawnorton.autocodec.OptionalEntry;
import com.bawnorton.autocodec.codec.adapter.entry.EntryAdapter;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;

/**
 * A field in a {@link ClassDeclNode} that should be included in a codec definition.
 */
public final class CodecEntryField extends ContextHolder {
    private final VariableDeclNode field;
    private final AnnotationNode optional;

    public CodecEntryField(ProcessingContext context, VariableDeclNode field) {
        super(context);
        this.field = field;
        this.optional = field.getAnnotation(OptionalEntry.class);

        if(field.getType().tsym.name.equals(names().any)) {
            throw new IllegalStateException("Cannot determine type for field \"" + field.getName() + "\", ensure the type is imported");
        }
    }

    public boolean isOptional() {
        return optional != null;
    }

    public VariableDeclNode field() {
        return field;
    }

    public Type getParameterType() {
        return fieldAdapter(field.getType()).getParameterType(field.getType());
    }

    public EntryAdapter getAdapter() {
        if (isOptional()) {
            return optionalEntryAdapter(field.getType(), optional);
        } else {
            return requiredEntryAdapter(field.getType());
        }
    }
}
