package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class RequiredMapEntryAdapter extends RequiredEntryAdapter {
    public RequiredMapEntryAdapter(ProcessingContext context) {
        super(context);
    }

    @Override
    protected MethodInvocationNode requiredInvocation(ClassDeclNode enclosingClass, VariableDeclNode field) {
        // Codec.unboundedMap(TYPE_A, TYPE_B)
        MethodInvocationNode unboundedMapInvocation = createUnboundedMapInvocation(field);
        // fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(unboundedMapInvocation, field);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation, enclosingClass, field);
    }

    private MethodInvocationNode createUnboundedMapInvocation(VariableDeclNode field) {
        FieldAccessNode unboundedMapReference = createUnboundedMapReference();
        List<Type> generics = field.getGenericTypes();
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
