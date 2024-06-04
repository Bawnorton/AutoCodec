package com.bawnorton.autocodec.nodes;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

// File
public final class CompilationUnitNode extends TreeNode {
    private final JCTree.JCCompilationUnit compilationUnit;
    private List<ImportNode> imports;
    private int importIndex;

    public CompilationUnitNode(JCTree.JCCompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;

        List<JCTree> definitions = compilationUnit.defs;

        List<ImportNode> imports = List.nil();
        importIndex = 0;
        for (JCTree definition : definitions) {
            if (definition.getKind() == Tree.Kind.IMPORT) {
                importIndex++;
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
