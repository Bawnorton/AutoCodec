package com.bawnorton.autocodec.nodes;

import com.sun.tools.javac.tree.JCTree;
import java.lang.annotation.Annotation;

public final class AnnotationNode extends ExpressionNode {
    private final JCTree.JCAnnotation annotation;

    public AnnotationNode(JCTree.JCAnnotation annotation) {
        this.annotation = annotation;
    }

    public JCTree.JCAnnotation getTree() {
        return annotation;
    }

    public String getQualifiedName() {
        JCTree.JCIdent ident = (JCTree.JCIdent) annotation.annotationType;
        return ident.sym.toString();
    }

    public boolean isOfType(Class<? extends Annotation> annotation) {
        return annotation.getName().equals(getQualifiedName());
    }
}
