package com.bawnorton.autocodec.codec.clazz;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.codec.clazz.factory.ClassCtorFactory;
import com.bawnorton.autocodec.codec.clazz.factory.CtorFactory;
import com.bawnorton.autocodec.codec.clazz.factory.RecordCtorFactory;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.MethodDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.node.container.FieldContainer;
import com.bawnorton.autocodec.node.container.MethodContainer;
import com.sun.tools.javac.util.List;

public final class CodecableClass extends ContextHolder implements MethodContainer, FieldContainer {
    private final ClassDeclNode node;
    private final List<CodecEntryField> entryFields;

    public CodecableClass(ProcessingContext context, ClassDeclNode node) {
        super(context);
        this.node = node;
        this.entryFields = initEntryFields();
    }

    private List<CodecEntryField> initEntryFields() {
        List<CodecEntryField> entryFields = List.nil();

        for (VariableDeclNode field : getFields()) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            entryFields = entryFields.append(new CodecEntryField(context, this, field));
        }

        return entryFields;
    }

    public ClassDeclNode node() {
        return node;
    }

    public String name() {
        return node.getName();
    }

    public List<MethodDeclNode> getMethods() {
        return node.getMethods();
    }

    public List<VariableDeclNode> getFields() {
        return node.getFields();
    }

    public List<CodecEntryField> getEntryFields() {
        return entryFields;
    }

    public void addMethod(MethodDeclNode method) {
        node.addMethod(method);
    }

    public void addField(VariableDeclNode field) {
        node.addField(field);
    }

    public CtorFactory getCtorFactory(ProcessingContext context) {
        if (node.isClass()) {
            return new ClassCtorFactory(context, node);
        } else if (node.isRecord()) {
            return new RecordCtorFactory(context, node);
        }
        throw new IllegalStateException("Cannot create Codec constructor for non-class/record type");
    }
}
