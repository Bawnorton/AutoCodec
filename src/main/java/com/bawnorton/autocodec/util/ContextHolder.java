package com.bawnorton.autocodec.util;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
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

    public void printNote(String message) {
        context.messager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    public void printWarning(String message) {
        context.messager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public void printError(String message) {
        context.messager().printMessage(Diagnostic.Kind.ERROR, message);
    }
}
