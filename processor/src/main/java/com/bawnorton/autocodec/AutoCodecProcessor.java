package com.bawnorton.autocodec;

import com.google.auto.service.AutoService;
import com.sun.source.tree.ClassTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import javax.annotation.processing.AbstractProcessor;
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
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Trees trees = Trees.instance(processingEnv);
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (!element.getKind().isClass()) continue;

                TreePath path = trees.getPath(element);
                ClassTree classTree = (ClassTree) path.getLeaf();
                ClassTree modifiedClassTree = addIntegerField(classTree);
                processingEnv.getMessager()
                        .printMessage(Diagnostic.Kind.NOTE, "Class modified: " + modifiedClassTree);
            }
        }
        return true;
    }

    private ClassTree addIntegerField(ClassTree classTree) {
        return classTree;
    }
}
