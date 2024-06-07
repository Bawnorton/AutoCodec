package com.bawnorton.autocodec.processor;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.tree.CodecAdder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.parser.ParserFactory;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("com.bawnorton.autocodec.AutoCodec")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AutoCodecProcessor extends AbstractProcessor {
    private ProcessingContext processingContext;
    private JavacTrees javacTrees;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.javacTrees = JavacTrees.instance(processingEnv);
        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        TreeMaker treeMaker = TreeMaker.instance(context);
        Names names = Names.instance(context);
        Symtab symtab = Symtab.instance(context);
        Types types = Types.instance(context);
        ParserFactory parserFactory = ParserFactory.instance(context);
        this.processingContext = new ProcessingContext(treeMaker, names, symtab, types, parserFactory, processingEnv.getMessager());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (!element.getKind().isClass()) continue;

                AutoCodec autoCodec = element.getAnnotation(AutoCodec.class);
                if (autoCodec == null) continue;

                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) javacTrees.getPath(element).getCompilationUnit();
                compilationUnit.accept(new CodecAdder(processingContext, autoCodec.name()));
                processingContext.messager().printMessage(Diagnostic.Kind.NOTE, "Processed: " + compilationUnit);
            }
        }
        return true;
    }
}
