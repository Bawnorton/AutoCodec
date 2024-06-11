package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class RequiredMapCodecEntryFactory extends RequiredCodecEntryFactory {
    public RequiredMapCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode) {
        super(context, enclosingClass, fieldNode);
    }

    @Override
    protected MethodInvocationNode requiredInvocation() {
        // Codec.unboundedMap(TYPE_A, TYPE_B)
        MethodInvocationNode unboundedMapInvocation = createUnboundedMapInvocation();
        // fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(unboundedMapInvocation);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation);
    }

    private MethodInvocationNode createUnboundedMapInvocation() {
        FieldAccessNode unboundedMapReference = createUnboundedMapReference();
        List<Type> generics = fieldNode.getGenericTypes();
        Type keyType = generics.head;
        Type valueType = generics.head;

        return MethodInvocationNode.builder(context)
                .methodSelect(unboundedMapReference)
                .argument(CodecLookup.lookupCodec(context, keyType))
                .argument(CodecLookup.lookupCodec(context, valueType))
                .build();
    }

    private FieldAccessNode createUnboundedMapReference() {
        return FieldAccessNode.builder(context)
                .selected("Codec")
                .name("unboundedMap")
                .build();
    }
}
