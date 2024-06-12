package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.GenericHelper;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;

public final class RequiredListEntryAdapter extends RequiredEntryAdapter {
    public RequiredListEntryAdapter(ProcessingContext context) {
        super(context);
    }

    @Override
    protected MethodInvocationNode requiredInvocation(ClassDeclNode enclosingClass, VariableDeclNode field) {
        // Codec.TYPE.listOf()
        MethodInvocationNode listOfInvocation = createListOfInvocation(field);
        // fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(listOfInvocation, field);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation, enclosingClass, field);
    }

    private MethodInvocationNode createListOfInvocation(VariableDeclNode field) {
        FieldAccessNode listOfReference = createListOfReference(field);

        return MethodInvocationNode.builder(context)
                .methodSelect(listOfReference)
                .build();
    }

    private FieldAccessNode createListOfReference(VariableDeclNode field) {
        Type generic = GenericHelper.getFirstGenericOfClassInParentsOrThrow(context, field.getType(), java.util.List.class);

        return FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, generic))
                .name("listOf")
                .build();
    }
}
