package com.bawnorton.autocodec.context;

import com.bawnorton.autocodec.codec.adapter.Adapters;
import com.bawnorton.autocodec.codec.adapter.entry.RequiredEntryAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.entry.OptionalEntryAdapter;
import com.bawnorton.autocodec.codec.adapter.entry.OptionalEntryAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.entry.RequiredEntryAdapter;
import com.bawnorton.autocodec.codec.adapter.field.FieldAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.field.FieldAdpater;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public class ContextHolder {
    protected final ProcessingContext context;

    public ContextHolder(ProcessingContext context) {
        this.context = context;
    }

    public ProcessingContext getContext() {
        return context;
    }

    public TreeMaker treeMaker() {
        return context.treeMaker();
    }

    public Names names() {
        return context.names();
    }

    public Symtab symtab() {
        return context.symtab();
    }

    public Types types() {
        return context.types();
    }

    public ParserFactory parserFactory() {
        return context.parserFactory();
    }

    public Enter enter() {
        return context.enter();
    }

    public JavacTrees trees() {
        return context.trees();
    }

    public Elements elements() {
        return context.elements();
    }

    public Adapters<FieldAdapterFactory> fieldAdapters() {
        return context.fieldAdapters();
    }

    public FieldAdpater fieldAdapter(Type fieldType) {
        return fieldAdapters().getAdapterFactory(context, fieldType).getAdapter(context);
    }

    public Adapters<RequiredEntryAdapterFactory> requiredEntryAdapters() {
        return context.requiredEntryAdapters();
    }

    public RequiredEntryAdapter requiredEntryAdapter(Type requiredType) {
        return requiredEntryAdapters().getAdapterFactory(context, requiredType).getAdapter(context);
    }

    public Adapters<OptionalEntryAdapterFactory> optionalEntryAdapters() {
        return context.optionalEntryAdapters();
    }

    public OptionalEntryAdapter optionalEntryAdapter(Type optionalType, AnnotationNode optional) {
        return optionalEntryAdapters().getAdapterFactory(context, optionalType).getAdapter(context, optional);
    }

    public void printNote(String message, Object... args) {
        context.messager().printMessage(Diagnostic.Kind.NOTE, message.formatted(args));
    }

    public void printWarning(String message, Object... args) {
        context.messager().printMessage(Diagnostic.Kind.WARNING, message.formatted(args));
    }

    public void printError(String message, Object... args) {
        context.messager().printMessage(Diagnostic.Kind.ERROR, message.formatted(args));
    }
}
