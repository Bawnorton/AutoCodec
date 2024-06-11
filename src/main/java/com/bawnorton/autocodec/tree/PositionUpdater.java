package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.List;

/**
 * Recompiles a modified compilation unit to update the positions of the nodes.
 */
public final class PositionUpdater extends TreeScanner {
    private final ContextHolder holder;

    public PositionUpdater(ProcessingContext context) {
        this.holder = new ContextHolder(context);
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit tree) {
        JavacParser parser = holder.parserFactory().newParser(tree.toString(), false, false, false);
        JCTree.JCCompilationUnit expected = parser.parseCompilationUnit();
        tree.accept(new PositionCopyingScanner(expected));
    }

    /**
     * Copy the positions from the expected tree to the actual tree.
     */
    private static class PositionCopyingScanner extends JCTree.Visitor {
        private JCTree expected;

        public PositionCopyingScanner(JCTree expected) {
            this.expected = expected;
        }
        
        private <T extends JCTree> void accept(T actual, T expected) {
            if (actual == null) return;
            
            this.expected = expected;
            actual.accept(this);
        }

        private <T extends JCTree> void accept(List<T> actual, List<T> expected) {
            if(actual == null || expected == null) return;
            
            for(int i = 0; i < actual.size(); i++) {
                accept(actual.get(i), expected.get(i));
            }
        }

        @Override
        public void visitTopLevel(JCTree.JCCompilationUnit that) {
            JCTree.JCCompilationUnit tree = (JCTree.JCCompilationUnit) expected;
            that.pos = tree.pos;

            accept(that.defs, tree.defs);
        }

        @Override
        public void visitPackageDef(JCTree.JCPackageDecl that) {
            JCTree.JCPackageDecl tree = (JCTree.JCPackageDecl) expected;
            that.pos = tree.pos;

            accept(that.annotations, tree.annotations);
            accept(that.pid, tree.pid);
        }

        @Override
        public void visitModuleDef(JCTree.JCModuleDecl that) {
            JCTree.JCModuleDecl tree = (JCTree.JCModuleDecl) expected;
            that.pos = tree.pos;

            accept(that.mods, tree.mods);
            accept(that.qualId, tree.qualId);
            accept(that.directives, tree.directives);
        }

        @Override
        public void visitExports(JCTree.JCExports that) {
            JCTree.JCExports tree = (JCTree.JCExports) expected;
            that.pos = tree.pos;

            accept(that.qualid, tree.qualid);
            accept(that.moduleNames, tree.moduleNames);
        }

        @Override
        public void visitOpens(JCTree.JCOpens that) {
            JCTree.JCOpens tree = (JCTree.JCOpens) expected;
            that.pos = tree.pos;

            accept(that.qualid, tree.qualid);
            accept(that.moduleNames, tree.moduleNames);
        }

        @Override
        public void visitProvides(JCTree.JCProvides that) {
            JCTree.JCProvides tree = (JCTree.JCProvides) expected;
            that.pos = tree.pos;

            accept(that.serviceName, tree.serviceName);
            accept(that.implNames, tree.implNames);
        }

        @Override
        public void visitRequires(JCTree.JCRequires that) {
            JCTree.JCRequires tree = (JCTree.JCRequires) expected;
            that.pos = tree.pos;

            accept(that.moduleName, tree.moduleName);
        }

        @Override
        public void visitUses(JCTree.JCUses that) {
            JCTree.JCUses tree = (JCTree.JCUses) expected;
            that.pos = tree.pos;

            accept(that.qualid, tree.qualid);
        }

        @Override
        public void visitImport(JCTree.JCImport that) {
            JCTree.JCImport tree = (JCTree.JCImport) expected;
            that.pos = tree.pos;

            accept(that.qualid, tree.qualid);
        }

        @Override
        public void visitClassDef(JCTree.JCClassDecl that) {
            if(that.getKind() == Tree.Kind.RECORD) return; // Record positions are handled by the parser

            JCTree.JCClassDecl tree = (JCTree.JCClassDecl) expected;
            that.pos = tree.pos;

            accept(that.mods, tree.mods);
            accept(that.typarams, tree.typarams);
            accept(that.extending, tree.extending);
            accept(that.implementing, tree.implementing);
            accept(that.permitting, tree.permitting);
            accept(that.defs, tree.defs);
        }

        @Override
        public void visitMethodDef(JCTree.JCMethodDecl that) {
            JCTree.JCMethodDecl tree = (JCTree.JCMethodDecl) expected;
            that.pos = tree.pos;

            accept(that.mods, tree.mods);
            accept(that.restype, tree.restype);
            accept(that.typarams, tree.typarams);
            accept(that.recvparam, tree.recvparam);
            accept(that.params, tree.params);
            accept(that.thrown, tree.thrown);
            accept(that.defaultValue, tree.defaultValue);
            accept(that.body, tree.body);
        }

        @Override
        public void visitVarDef(JCTree.JCVariableDecl that) {
            JCTree.JCVariableDecl tree = (JCTree.JCVariableDecl) expected;
            that.pos = tree.pos;

            accept(that.mods, tree.mods);
            accept(that.vartype, tree.vartype);
            accept(that.nameexpr, tree.nameexpr);
            accept(that.init, tree.init);
        }

        @Override
        public void visitSkip(JCTree.JCSkip that) {
        }

        @Override
        public void visitBlock(JCTree.JCBlock that) {
            JCTree.JCBlock tree = (JCTree.JCBlock) expected;
            that.pos = tree.pos;

            accept(that.stats, tree.stats);
        }

        @Override
        public void visitDoLoop(JCTree.JCDoWhileLoop that) {
            JCTree.JCDoWhileLoop tree = (JCTree.JCDoWhileLoop) expected;
            that.pos = tree.pos;

            accept(that.body, tree.body);
            accept(that.cond, tree.cond);
        }

        @Override
        public void visitWhileLoop(JCTree.JCWhileLoop that) {
            JCTree.JCWhileLoop tree = (JCTree.JCWhileLoop) expected;
            that.pos = tree.pos;

            accept(that.cond, tree.cond);
            accept(that.body, tree.body);
        }

        @Override
        public void visitForLoop(JCTree.JCForLoop that) {
            JCTree.JCForLoop tree = (JCTree.JCForLoop) expected;
            that.pos = tree.pos;

            accept(that.init, tree.init);
            accept(that.cond, tree.cond);
            accept(that.step, tree.step);
            accept(that.body, tree.body);
        }

        @Override
        public void visitForeachLoop(JCTree.JCEnhancedForLoop that) {
            JCTree.JCEnhancedForLoop tree = (JCTree.JCEnhancedForLoop) expected;
            that.pos = tree.pos;

            accept(that.var, tree.var);
            accept(that.expr, tree.expr);
            accept(that.body, tree.body);
        }

        @Override
        public void visitLabelled(JCTree.JCLabeledStatement that) {
            JCTree.JCLabeledStatement tree = (JCTree.JCLabeledStatement) expected;
            that.pos = tree.pos;

            accept(that.body, tree.body);
        }

        @Override
        public void visitSwitch(JCTree.JCSwitch that) {
            JCTree.JCSwitch tree = (JCTree.JCSwitch) expected;
            that.pos = tree.pos;

            accept(that.selector, tree.selector);
            accept(that.cases, tree.cases);
        }

        @Override
        public void visitCase(JCTree.JCCase that) {
            JCTree.JCCase tree = (JCTree.JCCase) expected;
            that.pos = tree.pos;

            accept(that.labels, tree.labels);
            accept(that.stats, tree.stats);
        }

        @Override
        public void visitSwitchExpression(JCTree.JCSwitchExpression that) {
            JCTree.JCSwitchExpression tree = (JCTree.JCSwitchExpression) expected;
            that.pos = tree.pos;

            accept(that.selector, tree.selector);
            accept(that.cases, tree.cases);
        }

        @Override
        public void visitSynchronized(JCTree.JCSynchronized that) {
            JCTree.JCSynchronized tree = (JCTree.JCSynchronized) expected;
            that.pos = tree.pos;

            accept(that.lock, tree.lock);
            accept(that.body, tree.body);
        }

        @Override
        public void visitTry(JCTree.JCTry that) {
            JCTree.JCTry tree = (JCTree.JCTry) expected;
            that.pos = tree.pos;

            accept(that.resources, tree.resources);
            accept(that.body, tree.body);
            accept(that.catchers, tree.catchers);
            accept(that.finalizer, tree.finalizer);
        }

        @Override
        public void visitCatch(JCTree.JCCatch that) {
            JCTree.JCCatch tree = (JCTree.JCCatch) expected;
            that.pos = tree.pos;

            accept(that.param, tree.param);
            accept(that.body, tree.body);
        }

        @Override
        public void visitConditional(JCTree.JCConditional that) {
            JCTree.JCConditional tree = (JCTree.JCConditional) expected;
            that.pos = tree.pos;

            accept(that.cond, tree.cond);
            accept(that.truepart, tree.truepart);
            accept(that.falsepart, tree.falsepart);
        }

        @Override
        public void visitIf(JCTree.JCIf that) {
            JCTree.JCIf tree = (JCTree.JCIf) expected;
            that.pos = tree.pos;

            accept(that.cond, tree.cond);
            accept(that.thenpart, tree.thenpart);
            accept(that.elsepart, tree.elsepart);
        }

        @Override
        public void visitExec(JCTree.JCExpressionStatement that) {
            JCTree.JCExpressionStatement tree = (JCTree.JCExpressionStatement) expected;
            that.pos = tree.pos;

            accept(that.expr, tree.expr);
        }

        public void visitBreak(JCTree.JCBreak that) {
        }

        public void visitYield(JCTree.JCYield that) {
            JCTree.JCYield tree = (JCTree.JCYield) this.expected;
            that.pos = tree.pos;
            
            accept(that.value, tree.value);
        }

        public void visitContinue(JCTree.JCContinue that) {
        }

        public void visitReturn(JCTree.JCReturn that) {
            JCTree.JCReturn tree = (JCTree.JCReturn) expected;
            that.pos = tree.pos;

            accept(that.expr, tree.expr);
        }

        public void visitThrow(JCTree.JCThrow that) {
            JCTree.JCThrow tree = (JCTree.JCThrow) expected;
            that.pos = tree.pos;

            accept(that.expr, tree.expr);
        }

        public void visitAssert(JCTree.JCAssert that) {
            JCTree.JCAssert tree = (JCTree.JCAssert) expected;
            that.pos = tree.pos;
            
            accept(that.cond, tree.cond);
            accept(that.detail, tree.detail);
        }

        public void visitApply(JCTree.JCMethodInvocation that) {
            JCTree.JCMethodInvocation tree = (JCTree.JCMethodInvocation) expected;
            that.pos = tree.pos;
            
            accept(that.typeargs, tree.typeargs);
            accept(that.meth, tree.meth);
            accept(that.args, tree.args);
        }

        public void visitNewClass(JCTree.JCNewClass that) {
            JCTree.JCNewClass tree = (JCTree.JCNewClass) expected;
            that.pos = tree.pos;
            
            accept(that.encl, tree.encl);
            accept(that.typeargs, tree.typeargs);
            accept(that.clazz, tree.clazz);
            accept(that.args, tree.args);
            accept(that.def, tree.def);
        }

        public void visitNewArray(JCTree.JCNewArray that) {
            JCTree.JCNewArray tree = (JCTree.JCNewArray) expected;
            that.pos = tree.pos;
            
            accept(that.annotations, tree.annotations);
            accept(that.elemtype, tree.elemtype);
            accept(that.dims, tree.dims);
            if(that.dimAnnotations != null && tree.dimAnnotations != null) {
                for(int i = 0; i < that.dimAnnotations.size(); i++) {
                    accept(that.dimAnnotations.get(i), tree.dimAnnotations.get(i));
                }
            }
            accept(that.elems, tree.elems);
        }

        public void visitLambda(JCTree.JCLambda that) {
            JCTree.JCLambda tree = (JCTree.JCLambda) expected;
            that.pos = tree.pos;
            
            accept(that.body, tree.body);
            accept(that.params, tree.params);
        }

        public void visitParens(JCTree.JCParens that) {
            JCTree.JCParens tree = (JCTree.JCParens) expected;
            that.pos = tree.pos;
            
            accept(that.expr, tree.expr);
        }

        public void visitAssign(JCTree.JCAssign that) {
            JCTree.JCAssign tree = (JCTree.JCAssign) expected;
            that.pos = tree.pos;
            
            accept(that.lhs, tree.lhs);
            accept(that.rhs, tree.rhs);
        }

        public void visitAssignop(JCTree.JCAssignOp that) {
            JCTree.JCAssignOp tree = (JCTree.JCAssignOp) expected;
            that.pos = tree.pos;
            
            accept(that.lhs, tree.lhs);
            accept(that.rhs, tree.rhs);
        }

        public void visitUnary(JCTree.JCUnary that) {
            JCTree.JCUnary tree = (JCTree.JCUnary) expected;
            that.pos = tree.pos;
            
            accept(that.arg, tree.arg);
        }

        public void visitBinary(JCTree.JCBinary that) {
            JCTree.JCBinary tree = (JCTree.JCBinary) expected;
            that.pos = tree.pos;
            
            accept(that.lhs, tree.lhs);
            accept(that.rhs, tree.rhs);
        }

        public void visitTypeCast(JCTree.JCTypeCast that) {
            JCTree.JCTypeCast tree = (JCTree.JCTypeCast) expected;
            that.pos = tree.pos;
            
            accept(that.clazz, tree.clazz);
            accept(that.expr, tree.expr);
        }

        public void visitTypeTest(JCTree.JCInstanceOf that) {
            JCTree.JCInstanceOf tree = (JCTree.JCInstanceOf) expected;
            that.pos = tree.pos;
            
            accept(that.expr, tree.expr);
            accept(that.pattern, tree.pattern);
        }

        public void visitBindingPattern(JCTree.JCBindingPattern that) {
            JCTree.JCBindingPattern tree = (JCTree.JCBindingPattern) expected;
            that.pos = tree.pos;
            
            accept(that.var, tree.var);
        }

        @Override
        public void visitDefaultCaseLabel(JCTree.JCDefaultCaseLabel that) {
        }

        @Override
        public void visitParenthesizedPattern(JCTree.JCParenthesizedPattern that) {
            JCTree.JCParenthesizedPattern tree = (JCTree.JCParenthesizedPattern) expected;
            that.pos = tree.pos;
            
            accept(that.pattern, tree.pattern);
        }

        @Override
        public void visitGuardPattern(JCTree.JCGuardPattern that) {
            JCTree.JCGuardPattern tree = (JCTree.JCGuardPattern) expected;
            that.pos = tree.pos;
            
            accept(that.patt, tree.patt);
            accept(that.expr, tree.expr);
        }

        public void visitIndexed(JCTree.JCArrayAccess that) {
            JCTree.JCArrayAccess tree = (JCTree.JCArrayAccess) expected;
            that.pos = tree.pos;
            
            accept(that.indexed, tree.indexed);
            accept(that.index, tree.index);
        }

        public void visitSelect(JCTree.JCFieldAccess that) {
            JCTree.JCFieldAccess tree = (JCTree.JCFieldAccess) expected;
            that.pos = tree.pos;
            
            accept(that.selected, tree.selected);
        }

        public void visitReference(JCTree.JCMemberReference that) {
            JCTree.JCMemberReference tree = (JCTree.JCMemberReference) expected;
            that.pos = tree.pos;
            
            accept(that.expr, tree.expr);
            accept(that.typeargs, tree.typeargs);
        }

        public void visitIdent(JCTree.JCIdent that) {
        }

        public void visitLiteral(JCTree.JCLiteral that) {
        }

        public void visitTypeIdent(JCTree.JCPrimitiveTypeTree that) {
        }

        public void visitTypeArray(JCTree.JCArrayTypeTree that) {
            JCTree.JCArrayTypeTree tree = (JCTree.JCArrayTypeTree) expected;
            that.pos = tree.pos;
            
            accept(that.elemtype, tree.elemtype);
        }

        public void visitTypeApply(JCTree.JCTypeApply that) {
            JCTree.JCTypeApply tree = (JCTree.JCTypeApply) expected;
            that.pos = tree.pos;
            
            accept(that.clazz, tree.clazz);
            accept(that.arguments, tree.arguments);
        }

        public void visitTypeUnion(JCTree.JCTypeUnion that) {
            JCTree.JCTypeUnion tree = (JCTree.JCTypeUnion) expected;
            that.pos = tree.pos;
            
            accept(that.alternatives, tree.alternatives);
        }

        public void visitTypeIntersection(JCTree.JCTypeIntersection that) {
            JCTree.JCTypeIntersection tree = (JCTree.JCTypeIntersection) expected;
            that.pos = tree.pos;
            
            accept(that.bounds, tree.bounds);
        }

        public void visitTypeParameter(JCTree.JCTypeParameter that) {
            JCTree.JCTypeParameter tree = (JCTree.JCTypeParameter) expected;
            that.pos = tree.pos;
            
            accept(that.annotations, tree.annotations);
            accept(that.bounds, tree.bounds);
        }

        @Override
        public void visitWildcard(JCTree.JCWildcard that) {
            JCTree.JCWildcard tree = (JCTree.JCWildcard) expected;
            that.pos = tree.pos;
            
            accept(that.kind, tree.kind);
            accept(that.inner, tree.inner);
        }

        @Override
        public void visitTypeBoundKind(JCTree.TypeBoundKind that) {
        }

        public void visitModifiers(JCTree.JCModifiers that) {
            JCTree.JCModifiers tree = (JCTree.JCModifiers) expected;
            that.pos = tree.pos;
            
            accept(that.annotations, tree.annotations);
        }

        public void visitAnnotation(JCTree.JCAnnotation that) {
            JCTree.JCAnnotation tree = (JCTree.JCAnnotation) expected;
            that.pos = tree.pos;
            
            accept(that.annotationType, tree.annotationType);
            accept(that.args, tree.args);
        }

        public void visitAnnotatedType(JCTree.JCAnnotatedType that) {
            JCTree.JCAnnotatedType tree = (JCTree.JCAnnotatedType) expected;
            that.pos = tree.pos;
            
            accept(that.annotations, tree.annotations);
            accept(that.underlyingType, tree.underlyingType);
        }

        public void visitErroneous(JCTree.JCErroneous that) {
        }

        public void visitLetExpr(JCTree.LetExpr that) {
            JCTree.LetExpr tree = (JCTree.LetExpr) expected;
            that.pos = tree.pos;
            
            accept(that.defs, tree.defs);
            accept(that.expr, tree.expr);
        }
    }
}