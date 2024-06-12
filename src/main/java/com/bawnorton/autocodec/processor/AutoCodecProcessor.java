package com.bawnorton.autocodec.processor;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.codec.adapter.Adapters;
import com.bawnorton.autocodec.codec.adapter.entry.*;
import com.bawnorton.autocodec.codec.adapter.field.FieldAdapterFactory;
import com.bawnorton.autocodec.codec.adapter.field.ListFieldAdpater;
import com.bawnorton.autocodec.codec.adapter.field.MapFieldAdpater;
import com.bawnorton.autocodec.codec.adapter.field.NormalFieldAdpater;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.tree.CodecAdder;
import com.bawnorton.autocodec.tree.PositionUpdater;
import com.bawnorton.autocodec.helper.TypeHelper;
import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Enter;
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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SupportedAnnotationTypes("com.bawnorton.autocodec.AutoCodec")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public class AutoCodecProcessor extends AbstractProcessor {
    private ProcessingContext processingContext;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        TreeMaker treeMaker = TreeMaker.instance(context);
        Names names = Names.instance(context);
        Symtab symtab = Symtab.instance(context);
        Types types = Types.instance(context);
        ParserFactory parserFactory = ParserFactory.instance(context);
        Enter enter = Enter.instance(context);
        JavacTrees trees = JavacTrees.instance(processingEnv);
        Elements elements = processingEnv.getElementUtils();

        this.processingContext = new ProcessingContext(
                treeMaker, names, symtab, types, parserFactory,
                enter, trees, elements, processingEnv.getMessager(),
                new Adapters<>(), new Adapters<>(), new Adapters<>()
        );

        initAdapters();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (!element.getKind().isClass()) continue;

                AutoCodec autoCodec = element.getAnnotation(AutoCodec.class);
                if (autoCodec == null) continue;

                JCTree.JCCompilationUnit compilationUnit = (JCTree.JCCompilationUnit) processingContext.trees().getPath(element).getCompilationUnit();
                compilationUnit.accept(new CodecAdder(processingContext, autoCodec.name()));
                compilationUnit.accept(new PositionUpdater(processingContext));
                processingContext.messager().printMessage(Diagnostic.Kind.NOTE, "Processed: " + compilationUnit);
            }
        }
        return true;
    }

    private void initAdapters() {
        Type listType = TypeHelper.typeOf(processingContext, List.class);
        Type mapType = TypeHelper.typeOf(processingContext, Map.class);

        Adapters<RequiredEntryAdapterFactory> requiredEntryAdapters = processingContext.requiredEntryAdapters();
        requiredEntryAdapters.setDefaultAdapterFactory(RequiredNormalEntryAdapter::new);
        requiredEntryAdapters.registerAdapterFactory(listType, RequiredListEntryAdapter::new);
        requiredEntryAdapters.registerAdapterFactory(mapType, RequiredMapEntryAdapter::new);

        Adapters<OptionalEntryAdapterFactory> optionalEntryAdapters = processingContext.optionalEntryAdapters();
        optionalEntryAdapters.setDefaultAdapterFactory(OptionalNormalEntryAdapter::new);
        optionalEntryAdapters.registerAdapterFactory(listType, OptionalListEntryAdapter::new);
        optionalEntryAdapters.registerAdapterFactory(mapType, OptionalMapEntryAdapter::new);

        Adapters<FieldAdapterFactory> fieldAdapters = processingContext.fieldAdapters();
        fieldAdapters.setDefaultAdapterFactory(NormalFieldAdpater::new);
        fieldAdapters.registerAdapterFactory(listType, ListFieldAdpater::new);
        fieldAdapters.registerAdapterFactory(mapType, MapFieldAdpater::new);
    }
}
