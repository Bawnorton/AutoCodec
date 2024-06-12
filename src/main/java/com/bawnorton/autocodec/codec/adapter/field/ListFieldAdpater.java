package com.bawnorton.autocodec.codec.adapter.field;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.GenericHelper;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.helper.TypeHelper;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class ListFieldAdpater extends FieldAdpater {
    public ListFieldAdpater(ProcessingContext context) {
        super(context);
    }

    @Override
    public Type getParameterType(Type fieldType) {
        return TypeHelper.findType(context, fieldType, java.util.List.class);
    }

    @Override
    public VariableDeclNode getParameter(FieldInfo field) {
        Type generic = GenericHelper.getFirstGenericOfClassInParentsOrThrow(context, field.getType(), java.util.List.class);
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(field.getName())
                .type("List")
                .genericParam(generic)
                .build();
    }

    @Override
    public List<StatementNode> createAssignmentStatements(FieldInfo field) {
        // this.fieldInfo
        FieldAccessNode fieldReference = FieldAccessNode.builder(context)
                .selected(IdentNode.of(context, "this"))
                .name(field.getName())
                .build();

        // fieldInfo is "List" exactly
        Type fieldType = field.getType();
        if (TypeHelper.is(fieldType, java.util.List.class)) {
            return List.of(AssignNode.builder(context)
                    .lhs(fieldReference)
                    .rhs(field.getName())
                    .build()
                    .toStatement(treeMaker()));
        } else {
            if (fieldType.isInterface()) {
                // TODO: Needs xmap support
                return List.nil();
            }

            List<Symbol> typeMembers = fieldType.tsym.getEnclosedElements();
            List<Symbol.MethodSymbol> ctors = typeMembers.stream()
                    .filter(member -> member instanceof Symbol.MethodSymbol)
                    .map(Symbol.MethodSymbol.class::cast)
                    .filter(member -> member.name.equals(names().init))
                    .collect(List.collector());

            Symbol.MethodSymbol noArgCtor = null;
            Symbol.MethodSymbol copyCtor = null;

            for (Symbol.MethodSymbol ctor : ctors) {
                if (ctor.params().isEmpty()) {
                    noArgCtor = ctor;
                    continue;
                }
                if (ctor.params().size() != 1) {
                    continue;
                }

                Symbol.VarSymbol ctorParam = ctor.params().head;
                Type paramType = ctorParam.type;
                if (!TypeHelper.isChildOf(context, paramType, java.util.List.class)) {
                    continue;
                }

                copyCtor = ctor;
            }

            if (copyCtor != null) {
                return createCopyCtorAssignment(field, fieldReference);
            } else if (noArgCtor != null) {
                return createAddAllAssignment(field, fieldReference);
            }

            throw new IllegalStateException("Cannot automatically create assignment for list type: " + fieldType + ". It does not have a no-arg ctor or copy ctor.");
        }
    }

    private List<StatementNode> createCopyCtorAssignment(FieldInfo field, FieldAccessNode fieldReference) {
        // new FieldType*(fieldInfo);
        NewClassNode newFieldType = NewClassNode.builder(context)
                .clazz(createNewFieldTypeClazz(field))
                .arg(field.getName())
                .build();

        // this.fieldInfo = new FieldType*(fieldInfo);
        return List.of(AssignNode.builder(context)
                .lhs(fieldReference)
                .rhs(newFieldType)
                .build()
                .toStatement(treeMaker()));
    }

    private List<StatementNode> createAddAllAssignment(FieldInfo field, FieldAccessNode fieldReference) {
        List<StatementNode> statements = List.nil();

        // new FieldType*();
        NewClassNode newFieldType = NewClassNode.builder(context)
                .clazz(createNewFieldTypeClazz(field))
                .build();

        // this.fieldInfo = new FieldType*();
        AssignNode fieldInit = AssignNode.builder(context)
                .lhs(fieldReference)
                .rhs(newFieldType)
                .build();

        statements = statements.append(fieldInit.toStatement(treeMaker()));

        // this.fieldInfo.addAll
        FieldAccessNode addAllReference = FieldAccessNode.builder(context)
                .selected(fieldReference)
                .name("addAll")
                .build();

        // this.fieldInfo.addAll(fieldInfo);
        MethodInvocationNode addAllInvocation = MethodInvocationNode.builder(context)
                .methodSelect(addAllReference)
                .argument(field.getName())
                .build();

        return statements.append(addAllInvocation.toStatement(treeMaker()));
    }

    private ExpressionNode createNewFieldTypeClazz(FieldInfo field) {
        List<Type> generics = field.getGenericTypes();
        ExpressionNode newFieldTypeClazz;
        if (generics == null) {
            // FieldType
            newFieldTypeClazz = IdentNode.of(context, field.getSimpleTypeName());
        } else {
            // FieldType<>
            newFieldTypeClazz = TypeApplyNode.builder(context)
                    .clazz(field.getSimpleTypeName())
                    .build();
        }
        return newFieldTypeClazz;
    }
}
