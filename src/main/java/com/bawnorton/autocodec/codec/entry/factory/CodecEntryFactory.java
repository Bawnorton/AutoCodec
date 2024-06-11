package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;

public abstract class CodecEntryFactory extends ContextHolder {
    protected final ClassDeclNode enclosingClass;
    protected final VariableDeclNode fieldNode;

    public CodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode) {
        super(context);
        this.enclosingClass = enclosingClass;
        this.fieldNode = fieldNode;
    }

    public abstract CodecEntry createEntry();

    /**
     * {@code [CodecDefintion].forGetter(Class::fieldName)}
     * @param supplier {@code [CodecDefinition]}
     */
    protected MethodInvocationNode getterInvocation(MethodInvocationNode supplier) {
        FieldAccessNode forGetterReference = FieldAccessNode.builder(context)
                .selected(supplier)
                .name("forGetter")
                .build();

        MemberReferenceNode selfFieldReference = MemberReferenceNode.builder(context)
                .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                .name(fieldNode.getName())
                .expression(enclosingClass.getName())
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(forGetterReference)
                .typeArgument(enclosingClass)
                .argument(selfFieldReference)
                .build();
    }
}
