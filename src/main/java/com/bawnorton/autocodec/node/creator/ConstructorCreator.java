package com.bawnorton.autocodec.node.creator;

import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import java.util.List;
import java.util.function.Supplier;

public abstract class ConstructorCreator {
    protected final ClassDeclNode classDeclNode;

    protected ConstructorCreator(ClassDeclNode classDeclNode) {
        this.classDeclNode = classDeclNode;
        if (!validate().get()) {
            throw new IllegalArgumentException("ConstructorCreator of type " + this.getClass().getSimpleName() + " is not valid for " + classDeclNode.getName());
        }
    }

    protected abstract Supplier<Boolean> validate();

    public abstract void createCtorForFields(ProcessingContext context, List<IncludedField> includedFields);
}
