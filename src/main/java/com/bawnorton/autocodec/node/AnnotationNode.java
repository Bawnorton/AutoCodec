package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.node.parser.ExpressionParser;
import com.sun.tools.javac.tree.JCTree;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

public final class AnnotationNode extends ExpressionNode {
    private final JCTree.JCAnnotation annotation;
    private final Map<String, ExpressionNode> args;

    public AnnotationNode(JCTree.JCAnnotation annotation) {
        this.annotation = annotation;
        this.args = new HashMap<>();

        for (JCTree.JCExpression arg : annotation.args) {
            if (!(arg instanceof JCTree.JCAssign assign)) continue;
            if (!(assign.lhs instanceof JCTree.JCIdent ident)) continue;
            JCTree.JCExpression value = assign.rhs;
            args.put(ident.name.toString(), ExpressionParser.asExpressionNode(value));
        }
    }

    public JCTree.JCAnnotation getTree() {
        return annotation;
    }

    public String getQualifiedName() {
        JCTree.JCIdent ident = (JCTree.JCIdent) annotation.annotationType;
        return ident.sym.toString();
    }

    public boolean hasArg(String name) {
        return args.containsKey(name);
    }

    public boolean hasValue() {
        return hasArg("value");
    }

    public <T extends ExpressionNode> T getArg(String name, Class<T> type) {
        return type.cast(args.get(name));
    }

    public <T extends ExpressionNode> T getValue(Class<T> type) {
        return getArg("value", type);
    }

    public boolean isOfType(Class<? extends Annotation> annotation) {
        return annotation.getName().equals(getQualifiedName());
    }
}
