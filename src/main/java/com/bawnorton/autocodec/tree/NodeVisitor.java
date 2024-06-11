package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.CompilationUnitNode;
import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;

/**
 * Tree visitor that wraps `JCTree` elements in `Node` objects.
 */
public abstract class NodeVisitor extends TreeScanner {
    protected final ContextHolder holder;

    protected NodeVisitor(ProcessingContext context) {
        this.holder = new ContextHolder(context);
    }

    protected void visitClassDeclNode(ClassDeclNode classDeclNode) {
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
    public final void visitTopLevel(JCTree.JCCompilationUnit tree) {
        CompilationUnitNode compilationUnitNode = new CompilationUnitNode(tree);
        visitCompilationUnitNode(compilationUnitNode);
        super.visitTopLevel(compilationUnitNode.getTree());
    }
}
