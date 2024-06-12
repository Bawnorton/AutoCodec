package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.AnnotationInfo;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public final class OptionalMapEntryAdapter extends OptionalEntryAdapter {
    public OptionalMapEntryAdapter(ProcessingContext context, AnnotationInfo optional) {
        super(context, optional);
    }

    protected MethodInvocationNode createOptionalInvocation(FieldInfo field) {
        // Codec.TYPE.optionalFieldOf("fieldName")
        return createOptionalFieldOfInvocation(CodecLookup.lookupCodec(context, field.getBoxedType(types())), field);
    }
}
