package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javax.lang.model.element.Modifier;

// Method
public final class MethodDeclNode extends TreeNode {
    private final JCTree.JCMethodDecl methodDecl;
    private final boolean isConstructor;

    private List<VariableDeclNode> parameters;

    public MethodDeclNode(JCTree.JCMethodDecl methodDecl) {
        this.methodDecl = methodDecl;
        this.isConstructor = methodDecl.getName().contentEquals("<init>");

        List<JCTree.JCVariableDecl> parameters = methodDecl.getParameters();
        List<VariableDeclNode> parameterNodes = List.nil();
        for (JCTree.JCVariableDecl parameter : parameters) {
            parameterNodes = parameterNodes.append(new VariableDeclNode(parameter));
        }
        this.parameters = parameterNodes;
    }

    public JCTree.JCMethodDecl getTree() {
        return methodDecl;
    }

    public boolean isStatic() {
        return methodDecl.getModifiers().getFlags().contains(Modifier.STATIC);
    }

    public boolean isConstructor() {
        return isConstructor;
    }

    public String getName() {
        return methodDecl.getName().toString();
    }

    public Type getReturnType() {
        JCTree returnType = methodDecl.getReturnType();
        return returnType == null ? Type.noType : returnType.type;
    }

    public List<VariableDeclNode> getParameters() {
        return parameters;
    }

    public void addParameter(VariableDeclNode parameter) {
        parameters = parameters.append(parameter);
        methodDecl.params = methodDecl.params.append(parameter.getTree());
    }

    public void setParameters(Iterable<VariableDeclNode> parameters) {
        this.parameters = List.nil();
        List<JCTree.JCVariableDecl> paramList = List.nil();
        for (VariableDeclNode parameter : parameters) {
            this.parameters = this.parameters.append(parameter);
            paramList = paramList.append(parameter.getTree());
        }
        methodDecl.params = paramList;
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private long flags;
        private Name name;
        private JCTree.JCExpression returnType;
        private List<JCTree.JCTypeParameter> genericParams = List.nil();
        private List<JCTree.JCVariableDecl> parameters = List.nil();
        private List<JCTree.JCExpression> thrown = List.nil();
        private JCTree.JCBlock body;
        private JCTree.JCExpression defaultValue;

        public Builder(ProcessingContext context) {
            super(context);
        }

        public Builder modifiers(long flags) {
            this.flags = flags;
            return this;
        }

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            this.name = names().fromString(name);
            return this;
        }

        public Builder returnType(JCTree.JCExpression returnType) {
            this.returnType = returnType;
            return this;
        }

        public Builder returnType(ExpressionNode returnType) {
            return returnType(returnType.getTree());
        }

        public Builder returnType(Type returnType) {
            return returnType(treeMaker().Type(returnType));
        }

        public Builder genericParam(JCTree.JCTypeParameter genericParam) {
            genericParams = genericParams.append(genericParam);
            return this;
        }

        public Builder param(JCTree.JCVariableDecl param) {
            parameters = parameters.append(param);
            return this;
        }

        public Builder param(VariableDeclNode param) {
            return param(param.getTree());
        }

        public Builder params(Iterable<VariableDeclNode> params) {
            List<JCTree.JCVariableDecl> paramList = List.nil();
            for (VariableDeclNode param : params) {
                paramList = paramList.append(param.getTree());
            }
            this.parameters = paramList;
            return this;
        }

        public Builder thrown(JCTree.JCExpression thrown) {
            this.thrown = this.thrown.append(thrown);
            return this;
        }

        public Builder body(JCTree.JCBlock body) {
            this.body = body;
            return this;
        }

        public Builder body(BlockNode body) {
            return body(body.getTree());
        }

        public Builder defaultValue(JCTree.JCExpression defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public MethodDeclNode build() {
            JCTree.JCModifiers modifiers = treeMaker().Modifiers(flags);
            JCTree.JCMethodDecl methodDecl = treeMaker().MethodDef(modifiers, name, returnType, genericParams, parameters, thrown, body, defaultValue);
            return new MethodDeclNode(methodDecl);
        }
    }
}
