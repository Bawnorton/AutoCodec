package com.bawnorton.autocodec.context;

import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import javax.annotation.processing.Messager;

public record ProcessingContext(TreeMaker treeMaker, Names names, Symtab symtab, Types types, ParserFactory parserFactory, Enter enter, JavacTrees trees, Messager messager) {
}
