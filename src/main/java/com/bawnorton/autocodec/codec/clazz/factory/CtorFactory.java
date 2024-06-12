package com.bawnorton.autocodec.codec.clazz.factory;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.MethodDeclNode;
import com.sun.tools.javac.util.List;
import java.util.function.Supplier;

public abstract class CtorFactory extends ContextHolder {
    protected final ClassDeclNode classDeclNode;

    protected CtorFactory(ProcessingContext context, ClassDeclNode classDeclNode) {
        super(context);
        this.classDeclNode = classDeclNode;
        if (!validate().get()) {
            throw new IllegalArgumentException("CtorFactory of type %s is not valid for %s".formatted(
                    this.getClass().getSimpleName(), classDeclNode.getSimpleNameString()
            ));
        }
    }

    protected abstract Supplier<Boolean> validate();

    public abstract MethodDeclNode createCtorForFields(List<CodecEntryField> codecEntryFields);
}
