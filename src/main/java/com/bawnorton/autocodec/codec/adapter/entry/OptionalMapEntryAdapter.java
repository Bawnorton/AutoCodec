package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public final class OptionalMapEntryAdapter extends OptionalEntryAdapter {
    public OptionalMapEntryAdapter(ProcessingContext context, AnnotationNode optionalAnnotation) {
        super(context, optionalAnnotation);
    }

    protected MethodInvocationNode createOptionalInvocation(VariableDeclNode field) {
        // Codec.TYPE.optionalFieldOf("fieldName")
        return createOptionalFieldOfInvocation(CodecLookup.lookupCodec(context, field.getBoxedType(types())), field);
    }
}
