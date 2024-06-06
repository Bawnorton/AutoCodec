package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;

public final class PositionUpdater extends ContextHolder {
    public PositionUpdater(ProcessingContext context) {
        super(context);
    }

    public void updatePositions(JCTree.JCClassDecl classDecl, int startPosition) {
        classDecl.accept(new PositionUpdatingScanner(), startPosition);
    }

    private static class PositionUpdatingScanner extends TreeScanner<Void, Integer> {
        @Override
        public Void visitClass(ClassTree node, Integer startPosition) {
            JCTree.JCClassDecl classDecl = (JCTree.JCClassDecl) node;
            classDecl.pos = startPosition;
            int newPosition = startPosition + 1;

            classDecl.mods.accept(this, newPosition);
            newPosition += classDecl.mods.toString().length();

            for (JCTree typeParameter : classDecl.typarams) {
                typeParameter.accept(this, newPosition);
                newPosition += typeParameter.toString().length();
            }

            if (classDecl.extending != null) {
                classDecl.extending.accept(this, newPosition);
                newPosition += classDecl.extending.toString().length();
            }

            for (JCTree implementing : classDecl.implementing) {
                implementing.accept(this, newPosition);
                newPosition += implementing.toString().length();
            }

            for (JCTree member : classDecl.defs) {
                member.accept(this, newPosition);
                newPosition += member.toString().length();
            }

            return null;
        }

        @Override
        public Void visitMethod(MethodTree node, Integer integer) {
            JCTree.JCMethodDecl methodDecl = (JCTree.JCMethodDecl) node;
            methodDecl.pos = integer;
            int newPosition = integer + 1;

            methodDecl.mods.accept(this, newPosition);
            newPosition += methodDecl.mods.toString().length();

            if (methodDecl.restype != null) {
                methodDecl.restype.accept(this, newPosition);
                newPosition += methodDecl.restype.toString().length();
            }

            for (JCTree typeParameter : methodDecl.typarams) {
                typeParameter.accept(this, newPosition);
                newPosition += typeParameter.toString().length();
            }

            for (JCTree parameter : methodDecl.params) {
                parameter.accept(this, newPosition);
                newPosition += parameter.toString().length();
            }

            if (methodDecl.defaultValue != null) {
                methodDecl.defaultValue.accept(this, newPosition);
                newPosition += methodDecl.defaultValue.toString().length();
            }

            if (methodDecl.body != null) {
                methodDecl.body.accept(this, newPosition);
            }

            return null;
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Integer integer) {
            JCTree.JCMethodInvocation methodInvocation = (JCTree.JCMethodInvocation) node;
            methodInvocation.pos = integer;
            int newPosition = integer + 1;

            methodInvocation.meth.accept(this, newPosition);
            newPosition += methodInvocation.meth.toString().length();

            for (JCTree argument : methodInvocation.args) {
                argument.accept(this, newPosition);
                newPosition += argument.toString().length();
            }

            return null;
        }

        @Override
        public Void visitBlock(BlockTree node, Integer integer) {
            JCTree.JCBlock block = (JCTree.JCBlock) node;
            block.pos = integer;
            int newPosition = integer;

            for (JCTree statement : block.stats) {
                statement.accept(this, newPosition);
                newPosition += statement.toString().length();
            }

            return null;
        }

        @Override
        public Void visitLambdaExpression(LambdaExpressionTree node, Integer integer) {
            JCTree.JCLambda lambda = (JCTree.JCLambda) node;
            lambda.pos = integer;
            int newPosition = integer;

            for (JCTree parameter : lambda.params) {
                parameter.accept(this, newPosition);
                newPosition += parameter.toString().length();
            }

            lambda.body.accept(this, newPosition);

            return null;
        }

        @Override
        public Void visitVariable(VariableTree node, Integer integer) {
            JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) node;
            variableDecl.pos = integer;
            int newPosition = integer;

            variableDecl.mods.accept(this, newPosition);
            newPosition += variableDecl.mods.toString().length();

            if (variableDecl.vartype != null) {
                variableDecl.vartype.accept(this, newPosition);
                newPosition += variableDecl.vartype.toString().length();
            }

            if (variableDecl.init != null) {
                variableDecl.init.accept(this, newPosition);
            }

            return null;
        }
    }
}