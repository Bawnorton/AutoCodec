package com.bawnorton.autocodec.info;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.IdentNode;
import com.bawnorton.autocodec.node.PrimitiveTypeTreeNode;
import com.bawnorton.autocodec.node.TypeApplyNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.node.parser.ExpressionParser;
import com.bawnorton.autocodec.util.Or;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import java.lang.annotation.Annotation;

public final class FieldInfo {
    private final Or<VariableDeclNode, Symbol.VarSymbol> field;

    public FieldInfo(VariableDeclNode field) {
        this.field = Or.left(field);
    }

    public FieldInfo(Symbol.VarSymbol field) {
        this.field = Or.right(field);
    }

    public boolean fromParent() {
        return field.isRight();
    }

    public boolean isPrimitive() {
        if(field.isLeft()) {
            return field.left().isPrimitive();
        } else {
            return field.right().type.isPrimitive();
        }
    }

    public boolean isStatic() {
        if(field.isLeft()) {
            return field.left().isStatic();
        } else {
            return field.right().isStatic();
        }
    }

    public String getNameString() {
        if (field.isLeft()) {
            return field.left().getNameString();
        } else {
            return field.right().name.toString();
        }
    }

    public Name getName() {
        if(field.isLeft()) {
            return field.left().getName();
        } else {
            return field.right().name;
        }
    }

    public Type getType() {
        if(field.isLeft()) {
            return field.left().getType();
        } else {
            return field.right().type;
        }
    }

    public ExpressionNode getVartype(ProcessingContext context) {
        if (field.isLeft()) {
            return ExpressionParser.asExpressionNode(field.left().getVartype());
        } else {
            Type fieldType = field.right().type;
            if (fieldType instanceof Type.ClassType classType) {
                IdentNode typeIdent = IdentNode.of(context, classType.tsym.name);
                if (!classType.isParameterized()) {
                    return typeIdent;
                }

                return TypeApplyNode.builder(context)
                        .clazz(typeIdent)
                        .typeArgs(classType.typarams_field.map(type -> type.tsym.name.toString()))
                        .build();
            } else if (fieldType instanceof Type.JCPrimitiveType primitiveType) {
                return PrimitiveTypeTreeNode.of(context, primitiveType.getTag());
            }
            throw new IllegalStateException("Unexpected type: " + fieldType);
        }
    }

    public Type getBoxedType(Types types) {
        if(field.isLeft()) {
            return field.left().getBoxedType(types);
        } else {
            return types.boxedTypeOrType(field.right().type);
        }
    }

    public List<Type> getGenericTypes() {
        if(field.isLeft()) {
            return field.left().getGenericTypes();
        } else {
            if(field.right().type instanceof Type.ClassType classType) {
                if(!classType.isParameterized()) return null;

                return classType.typarams_field;
            }
            return null;
        }
    }

    public Name getSimpleTypeName() {
        if (field.isLeft()) {
            return field.left().getSimpleTypeName();
        } else {
            return field.right().type.tsym.getSimpleName();
        }
    }

    public boolean sameNameAndType(Symbol.VarSymbol that) {
        if (field.isLeft()) {
            return field.left().sameNameAndType(that);
        } else {
            Type thisType = getType();
            Type thatType = that.type;

            String thisName = getNameString();
            String thatName = that.name.toString();

            return thisType.equals(thatType) && thisName.equals(thatName);
        }
    }

    public Symbol getOwner() {
        if (field.isLeft()) {
            return field.left().getOwner();
        } else {
            return field.right().owner;
        }
    }

    public boolean annotationPresent(Class<? extends Annotation> annotationClass) {
        if (field.isLeft()) {
            return field.left().annotationPresent(annotationClass);
        } else {
            return field.right().getAnnotation(annotationClass) != null;
        }
    }

    public AnnotationInfo getAnnotation(Class<? extends Annotation> annotationClass) {
        if(field.isLeft()) {
            AnnotationNode annotationNode = field.left().getAnnotation(annotationClass);
            if (annotationNode == null) return null;

            return new AnnotationInfo(field.left().getAnnotation(annotationClass));
        } else {
            Annotation annotation = field.right().getAnnotation(annotationClass);
            if (annotation == null) return null;

            return new AnnotationInfo(field.right().getAnnotation(annotationClass));
        }
    }
}
