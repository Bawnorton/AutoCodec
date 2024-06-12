package com.bawnorton.autocodec.context;

import com.bawnorton.autocodec.codec.adapter.Adapters;
import com.bawnorton.autocodec.codec.adapter.entry.RequiredEntryAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.entry.OptionalEntryAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.field.FieldAdapterFactory;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import javax.annotation.processing.Messager;
import javax.lang.model.util.Elements;

public record ProcessingContext(
        TreeMaker treeMaker,
        Names names,
        Symtab symtab,
        Types types,
        ParserFactory parserFactory,
        Enter enter,
        JavacTrees trees,
        Elements elements,
        Messager messager,
        Adapters<FieldAdapterFactory> fieldAdapters,
        Adapters<RequiredEntryAdapterFactory> requiredEntryAdapters,
        Adapters<OptionalEntryAdapterFactory> optionalEntryAdapters) {
}
