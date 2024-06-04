package com.bawnorton.autocodec.util;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;

public record ProcessingContext(TreeMaker treeMaker, Names names, Symtab symtab, Types types) {
}
