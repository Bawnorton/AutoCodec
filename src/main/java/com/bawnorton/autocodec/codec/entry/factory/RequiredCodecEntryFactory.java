package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.context.ProcessingContext;

public abstract class RequiredCodecEntryFactory extends CodecEntryFactory {
    protected RequiredCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode) {
        super(context, enclosingClass, fieldNode);
    }

    @Override
    public CodecEntry createEntry() {
        MethodInvocationNode requiredInvocation = requiredInvocation();
        return new CodecEntry(fieldNode, requiredInvocation);
    }

    protected abstract MethodInvocationNode requiredInvocation();

    protected MethodInvocationNode createFieldOfInvocation(ExpressionNode codecExression) {
        FieldAccessNode fieldOfReference = createFieldOfReference(codecExression);

        return MethodInvocationNode.builder(context)
                .methodSelect(fieldOfReference)
                .argument(LiteralNode.of(context, fieldNode.getName()))
                .build();
    }

    protected FieldAccessNode createFieldOfReference(ExpressionNode selected) {
        return FieldAccessNode.builder(context)
                .selected(selected)
                .name("fieldOf")
                .build();
    }
}
