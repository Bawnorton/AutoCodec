package com.bawnorton.autocodec.context;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.parser.ParserFactory;
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

    public ParserFactory parserFactory() {
        return context.parserFactory();
    }

    public Enter enter() {
        return context.enter();
    }

    public JavacTrees trees() {
        return context.trees();
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
