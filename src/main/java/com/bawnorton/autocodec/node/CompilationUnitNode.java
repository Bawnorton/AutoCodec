package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.node.finder.ImportFinder;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

// File
public final class CompilationUnitNode extends TreeNode implements ImportFinder {
    private final JCTree.JCCompilationUnit compilationUnit;
    private List<ImportNode> imports;
    private List<ClassDeclNode> classes;
    private int importIndex;

    public CompilationUnitNode(JCTree.JCCompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        List<JCTree> definitions = compilationUnit.defs;

        List<ImportNode> imports = List.nil();
        List<ClassDeclNode> classes = List.nil();
        importIndex = 0;
        for (JCTree definition : definitions) {
            if (definition instanceof JCTree.JCImport importTree) {
                importIndex++;
                imports = imports.append(new ImportNode(importTree));
            } else if (definition instanceof JCTree.JCClassDecl classDecl) {
                classes = classes.append(new ClassDeclNode(classDecl));
            }
        }
        this.imports = imports;
        this.classes = classes;
    }

    public JCTree.JCCompilationUnit getTree() {
        return compilationUnit;
    }

    public List<ImportNode> getImports() {
        return imports;
    }

    public List<ClassDeclNode> getClasses() {
        return classes;
    }

    public void addImport(ImportNode importNode) {
        imports = imports.append(importNode);

        List<JCTree> definitions = compilationUnit.defs;
        List<JCTree> newDefinitions = List.nil();
        for (int i = 0; i < importIndex; i++) {
            newDefinitions = newDefinitions.append(definitions.get(i));
        }
        newDefinitions = newDefinitions.append(importNode.getTree());
        for (int i = importIndex; i < definitions.size(); i++) {
            newDefinitions = newDefinitions.append(definitions.get(i));
        }
        importIndex++;
        compilationUnit.defs = newDefinitions;
    }
}
