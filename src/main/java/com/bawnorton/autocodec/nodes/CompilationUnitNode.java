package com.bawnorton.autocodec.nodes;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

// File
public final class CompilationUnitNode extends TreeNode {
    private final JCTree.JCCompilationUnit compilationUnit;
    private List<ImportNode> imports;

    public CompilationUnitNode(JCTree.JCCompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        List<JCTree> definitions = compilationUnit.defs;

        List<ImportNode> imports = List.nil();
        for (JCTree definition : definitions) {
            if (definition.getKind() == Tree.Kind.IMPORT) {
                imports = imports.append(new ImportNode((JCTree.JCImport) definition));
            }
        }
        this.imports = imports;
    }

    public JCTree.JCCompilationUnit getTree() {
        return compilationUnit;
    }

    public List<ImportNode> getImports() {
        return imports;
    }

    public void addImport(ImportNode importNode) {
        imports = imports.prepend(importNode);
        compilationUnit.defs = compilationUnit.defs.prepend(importNode.getImport());
    }
}
