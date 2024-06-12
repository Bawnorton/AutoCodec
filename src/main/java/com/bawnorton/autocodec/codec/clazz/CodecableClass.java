package com.bawnorton.autocodec.codec.clazz;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.codec.clazz.factory.ClassCtorFactory;
import com.bawnorton.autocodec.codec.clazz.factory.CtorFactory;
import com.bawnorton.autocodec.codec.clazz.factory.RecordCtorFactory;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.MethodDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.node.container.FieldContainer;
import com.bawnorton.autocodec.node.container.MethodContainer;
import com.sun.tools.javac.util.List;

public final class CodecableClass extends ContextHolder implements MethodContainer, FieldContainer {
    private final ClassDeclNode classDecl;
    private final List<CodecEntryField> entryFields;

    public CodecableClass(ProcessingContext context, ClassDeclNode classDecl) {
        super(context);
        this.classDecl = classDecl;
        this.entryFields = initEntryFields();
    }

    private List<CodecEntryField> initEntryFields() {
        List<CodecEntryField> entryFields = List.nil();

        for (VariableDeclNode field : getFields()) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            entryFields = entryFields.append(new CodecEntryField(context, field));
        }

        return entryFields;
    }

    public ClassDeclNode classDecl() {
        return classDecl;
    }

    public String name() {
        return classDecl.getName();
    }

    public List<MethodDeclNode> getMethods() {
        return classDecl.getMethods();
    }

    public List<VariableDeclNode> getFields() {
        return classDecl.getFields();
    }

    public List<CodecEntryField> getEntryFields() {
        return entryFields;
    }

    public void addMethod(MethodDeclNode method) {
        classDecl.addMethod(method);
    }

    public void addField(VariableDeclNode field) {
        classDecl.addField(field);
    }

    public CtorFactory getCtorFactory(ProcessingContext context) {
        if (classDecl.isClass()) {
            return new ClassCtorFactory(context, classDecl);
        } else if (classDecl.isRecord()) {
            return new RecordCtorFactory(context, classDecl);
        }
        throw new IllegalStateException("Cannot create Codec constructor for non-class/record type");
    }
}
