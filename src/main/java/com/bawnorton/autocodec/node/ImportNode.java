package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;

// Import
public final class ImportNode extends TreeNode {
    private final JCTree.JCImport importNode;

    public ImportNode(JCTree.JCImport importNode) {
        this.importNode = importNode;
    }

    public JCTree.JCImport getTree() {
        return importNode;
    }

    public String getImportPath() {
        return importNode.qualid.toString();
    }

    public boolean isStatic() {
        return importNode.isStatic();
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private String importPath;
        private boolean isStatic;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder importPath(String importPath) {
            this.importPath = importPath;
            return this;
        }

        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }

        public ImportNode build() {
            if(importPath == null) throw new IllegalStateException("importPath is required");

            Symbol.ModuleSymbol module = symtab().enterModule(names().fromString(""));
            Symbol.PackageSymbol packageSymbol = symtab().enterPackage(module, names().fromString(importPath));
            JCTree.JCExpression qualid = treeMaker().QualIdent(packageSymbol);
            JCTree.JCImport importNode = treeMaker().Import(qualid, isStatic);

            return new ImportNode(importNode);
        }
    }
}
