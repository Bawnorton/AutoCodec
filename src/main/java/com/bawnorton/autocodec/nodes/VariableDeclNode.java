package com.bawnorton.autocodec.nodes;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;

// Field
public final class VariableDeclNode extends TreeNode {
    private final JCTree.JCVariableDecl variableDecl;
    private final List<AnnotationNode> annotations;

    public VariableDeclNode(JCTree.JCVariableDecl variableDecl) {
        this.variableDecl = variableDecl;

        List<AnnotationNode> annotations = List.nil();
        for (JCTree.JCAnnotation annotation : variableDecl.mods.annotations) {
            annotations = annotations.append(new AnnotationNode(annotation));
        }
        this.annotations = annotations;
    }

    public JCTree.JCVariableDecl getTree() {
        return variableDecl;
    }

    public String getName() {
        return variableDecl.name.toString();
    }

    public Type getType() {
        return variableDecl.vartype.type;
    }

    public boolean isStatic() {
        return variableDecl.mods.getFlags().contains(Modifier.STATIC);
    }

    public boolean annotationPresent(Class<? extends Annotation> annotation) {
        return annotations.stream().anyMatch(annotationNode -> annotationNode.isOfType(annotation));
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression primitiveType;
        private Name typeName;
        private boolean implicit;
        private List<Name> genericParams;

        private long flags;
        private Name name;

        private JCTree.JCExpression initializer;

        private Builder(ProcessingContext context) {
            super(context);
        }

        public Builder type(Name typeName) {
            this.typeName = typeName;
            return this;
        }

        public Builder type(String typeName) {
            return type(names().fromString(typeName));
        }

        public Builder primitiveType(TypeTag typeTag) {
            this.primitiveType = treeMaker().TypeIdent(typeTag);
            return this;
        }

        public Builder implicitType() {
            this.implicit = true;
            return this;
        }

        public Builder genericParam(Name name) {
            if(genericParams == null) {
                genericParams = List.of(name);
            } else {
                genericParams = genericParams.append(name);
            }
            return this;
        }

        public Builder genericParam(String name) {
            return genericParam(names().fromString(name));
        }

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            return name(names().fromString(name));
        }

        public Builder modifiers(long flags) {
            this.flags = flags;
            return this;
        }

        public Builder initialValue(JCTree.JCExpression initializer) {
            this.initializer = initializer;
            return this;
        }

        public VariableDeclNode build() {
            if (name == null) throw new IllegalStateException("Name must be set");

            JCTree.JCExpression type;
            if(implicit) {
                type = null;
            } else if (primitiveType != null) {
                type = primitiveType;
            } else if (genericParams == null && typeName != null) {
                type = treeMaker().Ident(typeName);
            } else if (typeName != null) {
                List<JCTree.JCExpression> typeArgs = genericParams.stream().map(treeMaker()::Ident).collect(List::nil, List::append, List::appendList);
                type = treeMaker().TypeApply(treeMaker().Ident(typeName), typeArgs);
            } else {
                throw new IllegalStateException("Type must be set");
            }

            JCTree.JCVariableDecl varDecl = treeMaker().VarDef(treeMaker().Modifiers(flags), name, type, initializer);
            return new VariableDeclNode(varDecl);
        }
    }
}
