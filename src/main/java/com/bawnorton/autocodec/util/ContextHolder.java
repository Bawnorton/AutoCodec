package com.bawnorton.autocodec.util;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

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
}
