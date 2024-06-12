package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;

public abstract class RequiredEntryAdapter extends EntryAdapter {
    protected RequiredEntryAdapter(ProcessingContext context) {
        super(context);
    }

    @Override
    public CodecEntry createEntry(ClassDeclNode enclosingClass, FieldInfo field) {
        MethodInvocationNode requiredInvocation = requiredInvocation(enclosingClass, field);
        return new CodecEntry(field, requiredInvocation);
    }

    protected abstract MethodInvocationNode requiredInvocation(ClassDeclNode enclosingClass, FieldInfo field);

    protected MethodInvocationNode createFieldOfInvocation(ExpressionNode codecExression, FieldInfo field) {
        FieldAccessNode fieldOfReference = createFieldOfReference(codecExression);

        return MethodInvocationNode.builder(context)
                .methodSelect(fieldOfReference)
                .argument(LiteralNode.of(context, field.getNameString()))
                .build();
    }

    protected FieldAccessNode createFieldOfReference(ExpressionNode selected) {
        return FieldAccessNode.builder(context)
                .selected(selected)
                .name("fieldOf")
                .build();
    }
}
