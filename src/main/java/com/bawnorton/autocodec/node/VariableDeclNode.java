package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.context.ContextHolder;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;

// Field
public final class VariableDeclNode extends StatementNode {
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

    public Name getName() {
        return variableDecl.name;
    }

    public String getNameString() {
        return getName().toString();
    }

    /**
     * @return {@code FieldType}
     */
    public Type getType() {
        return variableDecl.vartype.type;
    }

    /**
     * @return {@code FieldType<Generics>}
     */
    public JCTree.JCExpression getVartype() {
        return variableDecl.vartype;
    }

    /**
     * @return {@code [Generics]}
     */
    public List<Type> getGenericTypes() {
        if (!canHaveGenerics()) {
            return null;
        }

        if(variableDecl.vartype.type instanceof Type.ClassType classType) {
            return classType.typarams_field;
        }
        return List.nil();
    }

    /**
     * @return {@code "FieldType"}
     */
    public Name getSimpleTypeName() {
        return variableDecl.vartype.type.tsym.name;
    }

    public Symbol getOwner() {
        return variableDecl.sym.owner;
    }

    public Type.ClassType getBoxedType(Types types) {
        return (Type.ClassType) types.boxedTypeOrType(getType());
    }

    public void setName(Name name) {
        getTree().name = name;
    }

    public boolean isStatic() {
        return variableDecl.mods.getFlags().contains(Modifier.STATIC);
    }

    public boolean isPrimitive() {
        return variableDecl.vartype.type.isPrimitive();
    }

    public boolean canHaveGenerics() {
        return variableDecl.vartype.type.isParameterized();
    }

    public boolean annotationPresent(Class<? extends Annotation> annotation) {
        return annotations.stream().anyMatch(annotationNode -> annotationNode.isOfType(annotation));
    }

    public AnnotationNode getAnnotation(Class<? extends Annotation> annotation) {
        return annotations.stream().filter(annotationNode -> annotationNode.isOfType(annotation)).findFirst().orElse(null);
    }

    public static Builder builder(ProcessingContext context) {
        return new Builder(context);
    }

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public boolean sameNameAndType(VariableDeclNode that) {
        Type thisType = getType();
        Type thatType = that.getType();

        String thisName = getNameString();
        String thatName = that.getNameString();

        return thisType.equals(thatType) && thisName.equals(thatName);
    }

    public boolean sameNameAndType(Symbol.VarSymbol that) {
        Type thisType = getType();
        Type thatType = that.type;

        String thisName = getNameString();
        String thatName = that.name.toString();

        return thisType.equals(thatType) && thisName.equals(thatName);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression primitiveType;
        private Name typeName;
        private Name enclosingTypeName;
        private List<JCTree.JCExpression> genericParams = null;

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

        public Builder type(Type type) {
            return type(type.tsym.getQualifiedName());
        }

        public Builder type (JCTree.JCClassDecl type) {
            return type(type.name);
        }

        public Builder type(ClassDeclNode type) {
            return type(type.getTree());
        }

        public Builder primitiveType(TypeTag typeTag) {
            this.primitiveType = treeMaker().TypeIdent(typeTag);
            return this;
        }

        public Builder enclosingType(Name enclosingTypeName) {
            this.enclosingTypeName = enclosingTypeName;
            return this;
        }

        public Builder enclosingType(String enclosingTypeName) {
            return enclosingType(names().fromString(enclosingTypeName));
        }

        public Builder genericParam(JCTree.JCExpression genericParam) {
            if (genericParams == null) {
                genericParams = List.nil();
            }
            genericParams = genericParams.append(genericParam);
            return this;
        }

        public Builder genericParam(ExpressionNode genericParam) {
            return genericParam(genericParam.getTree());
        }

        public Builder genericParam(Type genericParam) {
            return genericParam(IdentNode.of(context, genericParam.tsym.name));
        }

        public Builder genericParam(JCTree.JCClassDecl genericParam) {
            return genericParam(genericParam.sym.type);
        }

        public Builder genericParam(ClassDeclNode genericParam) {
            return genericParam(genericParam.getTree());
        }

        public Builder genericParams(List<Type> genericParams) {
            if (genericParams == null) {
                return this;
            }

            this.genericParams = genericParams.map(type -> IdentNode.of(context, type.tsym.name).getTree());
            return this;
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

        public Builder initialValue(ExpressionNode initializer) {
            return initialValue(initializer.getTree());
        }

        public VariableDeclNode build() {
            if (name == null) throw new IllegalStateException("Name must be set");

            JCTree.JCExpression type;
            if (primitiveType != null) {
                type = primitiveType;
            } else if (genericParams == null && typeName != null) {
                type = treeMaker().Ident(typeName);
            } else if (typeName != null) {
                JCTree.JCIdent typeIdent = treeMaker().Ident(typeName);
                if (enclosingTypeName != null) {
                    JCTree.JCIdent enclosingTypeIdent = treeMaker().Ident(enclosingTypeName);
                    type = treeMaker().TypeApply(
                            FieldAccessNode.builder(context)
                                    .selected(enclosingTypeIdent)
                                    .name(typeName)
                                    .build()
                                    .getTree(),
                            genericParams
                    );
                } else {
                    type = treeMaker().TypeApply(typeIdent, genericParams);
                }
            } else {
                throw new IllegalStateException("Type must be set");
            }

            JCTree.JCVariableDecl varDecl = treeMaker().VarDef(treeMaker().Modifiers(flags), name, type, initializer);
            return new VariableDeclNode(varDecl);
        }
    }
}
