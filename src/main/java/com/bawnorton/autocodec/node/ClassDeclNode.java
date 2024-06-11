package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.node.container.FieldContainer;
import com.bawnorton.autocodec.node.container.MethodContainer;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import java.lang.annotation.Annotation;

// Class, Enum, Interface, Record, Annotation
public final class ClassDeclNode extends StatementNode implements MethodContainer, FieldContainer {
    private final JCTree.JCClassDecl classDecl;
    private final Tree.Kind kind;

    private List<VariableDeclNode> fields;
    private List<MethodDeclNode> methods;
    private List<AnnotationNode> annotations;

    public ClassDeclNode(JCTree.JCClassDecl classDecl) {
        this.classDecl = classDecl;
        this.kind = classDecl.getKind();

        List<JCTree> definitions = classDecl.defs;

        List<VariableDeclNode> fields = List.nil();
        List<MethodDeclNode> methods = List.nil();

        for (JCTree definition : definitions) {
            if(definition.getKind() == Tree.Kind.VARIABLE) {
                fields = fields.append(new VariableDeclNode((JCTree.JCVariableDecl) definition));
            } else if (definition.getKind() == Tree.Kind.METHOD) {
                methods = methods.append(new MethodDeclNode((JCTree.JCMethodDecl) definition));
            }
        }

        this.fields = fields;
        this.methods = methods;

        List<AnnotationNode> annotations = List.nil();
        for (JCTree.JCAnnotation annotation : classDecl.mods.annotations) {
            annotations = annotations.append(new AnnotationNode(annotation));
        }
        this.annotations = annotations;
    }

    public JCTree.JCClassDecl getTree() {
        return classDecl;
    }

    public List<VariableDeclNode> getFields() {
        return fields;
    }

    public List<MethodDeclNode> getMethods() {
        return methods;
    }

    public List<MethodDeclNode> getConstructors() {
        List<MethodDeclNode> constructors = List.nil();
        for (MethodDeclNode method : methods) {
            if (method.isConstructor()) {
                constructors = constructors.append(method);
            }
        }
        return constructors;
    }

    public String getName() {
        return classDecl.getSimpleName().toString();
    }

    public Type.ClassType getType() {
        return (Type.ClassType) classDecl.sym.type;
    }

    public Type.ClassType getSuperClassType() {
        Type superClassType = classDecl.sym.getSuperclass();
        if (superClassType != Type.noType) {
            return (Type.ClassType) superClassType;
        }
        return null;
    }

    public boolean annotationPresent(Class<? extends Annotation> annotation) {
        return annotations.stream().anyMatch(annotationNode -> annotationNode.isOfType(annotation));
    }

    public void addMethod(MethodDeclNode method) {
        methods = methods.append(method);
        classDecl.defs = classDecl.defs.append(method.getTree());
    }

    public void addField(VariableDeclNode field) {
        fields = fields.prepend(field);
        classDecl.defs = classDecl.defs.prepend(field.getTree());
    }

    public boolean isRecord() {
        return kind == Tree.Kind.RECORD;
    }

    public boolean isAnnotation() {
        return kind == Tree.Kind.ANNOTATION_TYPE;
    }

    public boolean isClass() {
        return kind == Tree.Kind.CLASS;
    }

    public boolean isEnum() {
        return kind == Tree.Kind.ENUM;
    }

    public boolean isInterface() {
        return kind == Tree.Kind.INTERFACE;
    }
}
