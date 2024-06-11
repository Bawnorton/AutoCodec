package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.GenericHelper;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.util.TypeUtils;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class RequiredListCodecEntryFactory extends RequiredCodecEntryFactory {
    public RequiredListCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode) {
        super(context, enclosingClass, fieldNode);
    }

    @Override
    protected MethodInvocationNode requiredInvocation() {
        // Codec.TYPE.listOf()
        MethodInvocationNode listOfInvocation = createListOfInvocation();
        // fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(listOfInvocation);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation);
    }

    private MethodInvocationNode createListOfInvocation() {
        FieldAccessNode listOfReference = createListOfReference();

        return MethodInvocationNode.builder(context)
                .methodSelect(listOfReference)
                .build();
    }

    private FieldAccessNode createListOfReference() {
        Type generic = GenericHelper.getFirstGenericOfClassInParentsOrThrow(context, fieldNode.getType(), java.util.List.class);

        return FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, generic))
                .name("listOf")
                .build();
    }
}
