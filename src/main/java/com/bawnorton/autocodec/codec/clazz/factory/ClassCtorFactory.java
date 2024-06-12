package com.bawnorton.autocodec.codec.clazz.factory;

import com.bawnorton.autocodec.codec.adapter.field.FieldAdpater;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.InheritanceHelper;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.*;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.List;
import java.util.Comparator;
import java.util.function.Supplier;

public final class ClassCtorFactory extends CtorFactory {
    private final List<Symbol.MethodSymbol> parentCtors;

    public ClassCtorFactory(ProcessingContext context, ClassDeclNode classDeclNode) {
        super(context, classDeclNode);
        this.parentCtors = InheritanceHelper.getParentConstructors(context, classDeclNode);
    }

    @Override
    protected Supplier<Boolean> validate() {
        return classDeclNode::isClass;
    }

    @Override
    public MethodDeclNode createCtorForFields(List<CodecEntryField> codecEntryFields) {
        List<CodecEntryField> parentFields = List.nil();
        List<CodecEntryField> ownFields = List.nil();

        for (CodecEntryField codecEntryField : codecEntryFields) {
            if(codecEntryField.fromParent()) {
                parentFields = parentFields.append(codecEntryField);
            } else {
                ownFields = ownFields.append(codecEntryField);
            }
        }

        MethodInvocationNode superCall = findPartialParentCtor(parentFields);
        if(superCall != null) {
            return createPartialParentCallingCtor(superCall, codecEntryFields, ownFields);
        }
        superCall = findMinArgParentConstructor();
        if (superCall != null) {
            // super(args) then assign fields
            return createPartialParentCallingCtor(superCall, ownFields, ownFields);
        }
        // assign fields
        return createFieldAssigningCtor(ownFields);
    }

    private MethodDeclNode createFieldAssigningCtor(List<CodecEntryField> fields) {
        BlockNode.Builder bodyBuilder = BlockNode.builder(context);
        addFieldsToCtorBody(fields, bodyBuilder);
        BlockNode body = bodyBuilder.build();

        // public ClassName(fields) { this.fieldInfo = fieldInfo; ... }
        return MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(names().init)
                .params(fieldsToParameters(fields))
                .body(body)
                .build();
    }

    private MethodDeclNode createPartialParentCallingCtor(MethodInvocationNode superCall, List<CodecEntryField> params, List<CodecEntryField> fields) {
        // super(args)
        BlockNode.Builder bodyBuilder = BlockNode.builder(context)
                .statement(superCall.toStatement(treeMaker()));
        addFieldsToCtorBody(fields, bodyBuilder);
        BlockNode body = bodyBuilder.build();

        // private ClassName(parentArgs, fields) { super(parentArgs); this.fieldInfo = fieldInfo; ... }
        return MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(names().init)
                .params(fieldsToParameters(params))
                .body(body)
                .build();
    }

    private void addFieldsToCtorBody(List<CodecEntryField> codecEntryFields, BlockNode.Builder bodyBuilder) {
        for (CodecEntryField codecEntryField : codecEntryFields) {
            FieldInfo fieldNode = codecEntryField.fieldInfo();
            FieldAdpater fieldAdpater = fieldAdapter(fieldNode.getType());
            bodyBuilder.statements(fieldAdpater.createAssignmentStatements(fieldNode));
        }
    }

    /**
     * Find the parent constructor with the least number of arguments.
     * <br>eg:
     * <pre>
     *     {@code
     *     public class Child extends Parent {
     *          private String s1;
     *          private String s2;
     *     }
     *
     *     public class Parent {
     *          private Integer i1;
     *          private String s1;
     *
     *          public Parent(Integer i1, String s1) {
     *              this.i1 = i1;
     *              this.s1 = s1;
     *          }
     *
     *          public Parent(Integer i1) {
     *              this.i1 = i1;
     *          }
     *
     *          public Parent() {}
     *      }
     *      }
     * </pre>
     * creates the invocation {@code super()} for the {@code Child} constructor.
     * If the no-arg constructor is not found, the constructor with the least number of arguments is used.
     * @return the invocation fieldInfo for the parent constructor or {@code null} if no matching constructor is found.
     */
    private MethodInvocationNode findMinArgParentConstructor() {
        Symbol.MethodSymbol minArgCtor = parentCtors.stream()
                .min(Comparator.comparingInt(methodSymbol -> methodSymbol.getParameters().size()))
                .orElse(null);
        if (minArgCtor == null) return null;

        List<ExpressionNode> arguments = minArgCtor.getParameters().map(parameter -> LiteralNode.defaultLiteral(context, parameter.type));
        // super(args)
        return MethodInvocationNode.builder(context)
                .methodSelect("super")
                .arguments(arguments)
                .build();
    }

    /**
     * Given a list of fields, find the parent constructor that references some of the fields as parameters.
     * Matching fields to parameters is done by type and name.
     * <br>eg:
     * <pre>
     * {@code
     * public class Child extends Parent {
     *     private String s1;
     *     private String s2;
     * }
     *
     * public class Parent {
     *     private Integer i1;
     *     private String s1;
     *
     *     public Parent(Integer i1, String s1) {
     *          this.i1 = i1;
     *          this.s1 = s1;
     *     }
     * }
     * }
     * </pre>
     * creates the invocation {@code super(null, s1)} for the {@code Child} constructor but is mindful of the default
     * value of the non-matching fields.
     * <br>ie:
     * if a {@code Parent} fieldInfo is of type {@code int} then instead of passing {@code null} as the argument, {@code 0} is passed.
     *
     * @return the invocation node for the parent constructor or {@code null} if no matching constructor is found.
     */
    private MethodInvocationNode findPartialParentCtor(List<CodecEntryField> parentFields) {
        for (Symbol.MethodSymbol parentCtor : parentCtors) {
            List<Symbol.VarSymbol> parameters = parentCtor.getParameters();
            List<ExpressionNode> arguments = List.nil();
            for (Symbol.VarSymbol parameter : parameters) {
                boolean found = false;
                for (CodecEntryField parentField : parentFields) {
                    FieldInfo fieldInfo = parentField.fieldInfo();
                    if (!fieldInfo.sameNameAndType(parameter)) continue;

                    arguments = arguments.append(IdentNode.of(context, parentCtor.owner.name.append(names().fromString("_")).append(fieldInfo.getName())));
                    found = true;
                    break;
                }
                if (found) continue;

                arguments = arguments.append(LiteralNode.defaultLiteral(context, parameter.type));
            }

            return MethodInvocationNode.builder(context)
                    .methodSelect("super")
                    .arguments(arguments)
                    .build();
        }
        return null;
    }

    public List<VariableDeclNode> fieldsToParameters(List<CodecEntryField> codecEntryFields) {
        return codecEntryFields.map(field -> {
            FieldInfo fieldInfo = field.fieldInfo();
            FieldAdpater fieldAdpater = fieldAdapter(fieldInfo.getType());
            VariableDeclNode parameter = fieldAdpater.getParameter(fieldInfo);
            if(field.fromParent()) {
                parameter.setName(fieldInfo.getOwner().name.append(names().fromString("_")).append(fieldInfo.getName()));
            }
            return parameter;
        });
    }
}
