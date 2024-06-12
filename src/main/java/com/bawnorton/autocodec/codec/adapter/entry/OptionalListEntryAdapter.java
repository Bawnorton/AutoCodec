package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.GenericHelper;
import com.bawnorton.autocodec.info.AnnotationInfo;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;

public final class OptionalListEntryAdapter extends OptionalEntryAdapter {
    public OptionalListEntryAdapter(ProcessingContext context, AnnotationInfo optional) {
        super(context, optional);
    }

    protected MethodInvocationNode createOptionalInvocation(FieldInfo field) {
        // Codec.TYPE.listOf()
        MethodInvocationNode listOfInvocation = createListOfInvocation(field);
        // optionalFieldOf("fieldName")
        return createOptionalFieldOfInvocation(listOfInvocation, field);
    }

    private MethodInvocationNode createListOfInvocation(FieldInfo field) {
        FieldAccessNode listOfReference = createListOfReference(field);

        return MethodInvocationNode.builder(context)
                .methodSelect(listOfReference)
                .build();
    }

    private FieldAccessNode createListOfReference(FieldInfo field) {
        Type generic = GenericHelper.getFirstGenericOfClassInParentsOrThrow(context, field.getType(), java.util.List.class);

        return FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, generic))
                .name("listOf")
                .build();
    }
}
