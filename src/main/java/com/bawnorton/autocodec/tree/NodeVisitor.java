package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.nodes.ClassDeclNode;
import com.bawnorton.autocodec.nodes.CompilationUnitNode;
import com.bawnorton.autocodec.nodes.ImportNode;
import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeTranslator;

public abstract class NodeVisitor extends TreeTranslator {
    protected final ContextHolder holder;

    protected NodeVisitor(ProcessingContext context) {
        this.holder = new ContextHolder(context);
    }

    protected void visitClassDeclNode(ClassDeclNode classDeclNode) {
    }

    protected void visitImportNode(ImportNode importNode) {
    }

    protected void visitCompilationUnitNode(CompilationUnitNode compilationUnitNode) {
    }

    @Override
    public final void visitClassDef(JCTree.JCClassDecl tree) {
        ClassDeclNode classDeclNode = new ClassDeclNode(tree);
        visitClassDeclNode(classDeclNode);
        super.visitClassDef(classDeclNode.getTree());
    }

    @Override
    public final void visitImport(JCTree.JCImport tree) {
        ImportNode importNode = new ImportNode(tree);
        visitImportNode(importNode);
        super.visitImport(importNode.getTree());
    }

    @Override
    public final void visitTopLevel(JCTree.JCCompilationUnit tree) {
        CompilationUnitNode compilationUnitNode = new CompilationUnitNode(tree);
        visitCompilationUnitNode(compilationUnitNode);
        super.visitTopLevel(compilationUnitNode.getTree());
    }
}
