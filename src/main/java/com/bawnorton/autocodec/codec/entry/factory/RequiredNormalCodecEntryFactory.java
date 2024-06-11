package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public final class RequiredNormalCodecEntryFactory extends RequiredCodecEntryFactory {
    public RequiredNormalCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode) {
        super(context, enclosingClass, fieldNode);
    }

    @Override
    protected MethodInvocationNode requiredInvocation() {
        // Codec.TYPE.fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(CodecLookup.lookupCodec(context, fieldNode.getBoxedType(types())));
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation);
    }
}
