package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;

public abstract class RequiredEntryAdapter extends EntryAdapter {
    protected RequiredEntryAdapter(ProcessingContext context) {
        super(context);
    }

    @Override
    public CodecEntry createEntry(ClassDeclNode enclosingClass, VariableDeclNode field) {
        MethodInvocationNode requiredInvocation = requiredInvocation(enclosingClass, field);
        return new CodecEntry(field, requiredInvocation);
    }

    protected abstract MethodInvocationNode requiredInvocation(ClassDeclNode enclosingClass, VariableDeclNode field);

    protected MethodInvocationNode createFieldOfInvocation(ExpressionNode codecExression, VariableDeclNode field) {
        FieldAccessNode fieldOfReference = createFieldOfReference(codecExression);

        return MethodInvocationNode.builder(context)
                .methodSelect(fieldOfReference)
                .argument(LiteralNode.of(context, field.getName()))
                .build();
    }

    protected FieldAccessNode createFieldOfReference(ExpressionNode selected) {
        return FieldAccessNode.builder(context)
                .selected(selected)
                .name("fieldOf")
                .build();
    }
}
