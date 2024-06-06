package com.bawnorton.autocodec.node;

import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.ProcessingContext;
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

    public String getName() {
        return variableDecl.name.toString();
    }

    public Type getType() {
        return variableDecl.vartype.type;
    }

    public JCTree.JCExpression getVarType() {
        return variableDecl.vartype;
    }

    public Type.ClassType getBoxedType(Types types) {
        return (Type.ClassType) types.boxedTypeOrType(getType());
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

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public boolean sameNameAndType(VariableDeclNode that) {
        Type thisType = getType();
        Type thatType = that.getType();

        String thisName = getName();
        String thatName = that.getName();

        return thisType.equals(thatType) && thisName.equals(thatName);
    }

    public boolean sameNameAndType(Symbol.VarSymbol that) {
        Type thisType = getType();
        Type thatType = that.type;

        String thisName = getName();
        String thatName = that.name.toString();

        return thisType.equals(thatType) && thisName.equals(thatName);
    }

    public static class Builder extends ContextHolder {
        private JCTree.JCExpression primitiveType;
        private Name typeName;
        private Name enclosingTypeName;
        private List<JCTree.JCClassDecl> genericParams = null;
        private boolean implicitType;

        private long flags;
        private Name name;
        private Name module;

        private boolean noSym;
        private Symbol.ClassSymbol ownerSym;

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

        public Builder implicitType() {
            this.implicitType = true;
            return this;
        }

        public Builder genericParam(JCTree.JCClassDecl genericParam) {
            if (genericParams == null) {
                genericParams = List.nil();
            }
            genericParams = genericParams.append(genericParam);
            return this;
        }

        public Builder genericParam(ClassDeclNode genericParam) {
            return genericParam(genericParam.getTree());
        }

        public Builder name(Name name) {
            this.name = name;
            return this;
        }

        public Builder name(String name) {
            return name(names().fromString(name));
        }

        public Builder module(Name module) {
            this.module = module;
            return this;
        }

        public Builder module(String module) {
            return module(names().fromString(module));
        }

        public Builder noSym() {
            this.noSym = true;
            return this;
        }

        public Builder owner(JCTree.JCClassDecl owner) {
            ownerSym = owner.sym;
            return this;
        }

        public Builder owner(ClassDeclNode owner) {
            return owner(owner.getTree());
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
            Symbol.VarSymbol sym = null;
            if (implicitType) {
                type = null;
            } else if (primitiveType != null) {
                type = primitiveType;
            } else if (genericParams == null && typeName != null) {
                type = treeMaker().Ident(typeName);
            } else if (typeName != null) {
                List<JCTree.JCExpression> genericArgs = List.nil();
                for (JCTree.JCClassDecl genericParam : genericParams) {
                    genericArgs = genericArgs.append(treeMaker().Ident(genericParam.name));
                }
                JCTree.JCIdent typeIdent = treeMaker().Ident(typeName);
                if (enclosingTypeName != null) {
                    JCTree.JCIdent enclosingTypeIdent = treeMaker().Ident(enclosingTypeName);
                    type = treeMaker().TypeApply(
                            FieldAccessNode.builder(context)
                                    .selected(enclosingTypeIdent)
                                    .name(typeName)
                                    .build()
                                    .getTree(),
                            genericArgs
                    );
                } else {
                    type = treeMaker().TypeApply(typeIdent, genericArgs);
                }

                if (!noSym) {
                    if (module == null) {
                        throw new IllegalStateException("Module must be set or noSym must be true");
                    }
                    if (ownerSym == null) {
                        throw new IllegalStateException("Owner must be set or noSym must be true");
                    }

                    Name className = module;
                    if (enclosingTypeName != null) {
                        className = className
                                .append(names().fromString("."))
                                .append(enclosingTypeName);
                    }
                    className = className
                            .append(names().fromString("."))
                            .append(typeName);
                    Symbol.ModuleSymbol moduleSymbol = symtab().enterModule(module);
                    Symbol.ClassSymbol classSymbol = symtab().enterClass(moduleSymbol, className);
                    List<Type> typeArgs = List.nil();
                    for (JCTree.JCClassDecl genericParam : genericParams) {
                        typeArgs = typeArgs.append(genericParam.sym.type);
                    }

                    Type enclosingType;
                    if (enclosingTypeName != null) {
                        enclosingType = symtab().enterClass(moduleSymbol, module.append(names().fromString(".")).append(enclosingTypeName)).type;
                    } else {
                        enclosingType = Type.noType;
                    }
                    Type.ClassType classType = new Type.ClassType(
                            enclosingType,
                            typeArgs,
                            classSymbol
                    );
                    type.type = classType;
                    JCTree.JCTypeApply applyType = (JCTree.JCTypeApply) type;
                    JCTree.JCExpression clazzType = applyType.clazz;
                    if(clazzType instanceof JCTree.JCIdent ident) {
                        ident.sym = classSymbol;
                    } else if(clazzType instanceof JCTree.JCFieldAccess fieldAccess) {
                        fieldAccess.sym = classSymbol;
                    }
                    applyType.clazz.type = new Type.ClassType(Type.noType, List.nil(), classSymbol);

                    sym = new Symbol.VarSymbol(flags, name, classType, ownerSym);
                }
            } else {
                throw new IllegalStateException("Type must be set");
            }

            JCTree.JCVariableDecl varDecl = treeMaker().VarDef(treeMaker().Modifiers(flags), name, type, initializer);
            if (sym != null) {
                varDecl.sym = sym;
            }
            return new VariableDeclNode(varDecl);
        }
    }
}
