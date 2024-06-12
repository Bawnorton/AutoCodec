package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.FieldAccessNode;
import com.bawnorton.autocodec.node.MemberReferenceNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.sun.source.tree.MemberReferenceTree;

public abstract class EntryAdapter extends ContextHolder {
    public EntryAdapter(ProcessingContext context) {
        super(context);
    }

    public abstract CodecEntry createEntry(ClassDeclNode enclosingClass, FieldInfo field);

    /**
     * {@code [CodecDefintion].forGetter(Class::fieldName)}
     */
    protected MethodInvocationNode getterInvocation(MethodInvocationNode supplier, ClassDeclNode enclosingClass, FieldInfo field) {
        FieldAccessNode forGetterReference = FieldAccessNode.builder(context)
                .selected(supplier)
                .name("forGetter")
                .build();

        MemberReferenceNode selfFieldReference = MemberReferenceNode.builder(context)
                .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                .name(field.getNameString())
                .expression(enclosingClass.getName())
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(forGetterReference)
                .typeArgument(enclosingClass)
                .argument(selfFieldReference)
                .build();
    }
}
