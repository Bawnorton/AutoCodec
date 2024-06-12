package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public final class RequiredNormalEntryAdapter extends RequiredEntryAdapter {
    public RequiredNormalEntryAdapter(ProcessingContext context) {
        super(context);
    }

    @Override
    protected MethodInvocationNode requiredInvocation(ClassDeclNode enclosingClass, FieldInfo field) {
        // Codec.TYPE.fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(CodecLookup.lookupCodec(context, field.getBoxedType(types())), field);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation, enclosingClass, field);
    }
}
