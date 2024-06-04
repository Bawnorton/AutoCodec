package com.bawnorton.autocodec.nodes;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

// Class, Enum, Interface, Record, Annotation
public final class ClassDeclNode extends TreeNode {
    private final JCTree.JCClassDecl classDecl;
    private final Tree.Kind kind;
    private final List<VariableDeclNode> fields;

    public ClassDeclNode(JCTree.JCClassDecl classDecl) {
        this.classDecl = classDecl;
        this.kind = classDecl.getKind();

        List<JCTree> definitions = classDecl.defs;

        List<VariableDeclNode> fields = List.nil();
        for (JCTree definition : definitions) {
            if(definition.getKind() == Tree.Kind.VARIABLE) {
                fields = fields.append(new VariableDeclNode((JCTree.JCVariableDecl) definition));
            }
        }
        this.fields = fields;
    }

    public JCTree.JCClassDecl getTree() {
        return classDecl;
    }

    public List<VariableDeclNode> getFields() {
        return fields;
    }

    public void addField(VariableDeclNode field) {
        fields.add(field);
        classDecl.defs = classDecl.defs.prepend(field.getTree());
    }

    public void addImport() {}

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
