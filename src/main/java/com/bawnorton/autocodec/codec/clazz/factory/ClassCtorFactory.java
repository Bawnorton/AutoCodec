package com.bawnorton.autocodec.codec.clazz.factory;

import com.bawnorton.autocodec.codec.adapter.field.FieldAdpater;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.BlockNode;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.bawnorton.autocodec.node.ExpressionNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.node.MethodDeclNode;
import com.bawnorton.autocodec.node.MethodInvocationNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import java.util.Comparator;
import java.util.function.Supplier;

public final class ClassCtorFactory extends CtorFactory {
    private final List<Symbol.MethodSymbol> parentCtors;

    public ClassCtorFactory(ProcessingContext context, ClassDeclNode classDeclNode) {
        super(context, classDeclNode);
        this.parentCtors = initParentCtors();
    }

    @Override
    protected Supplier<Boolean> validate() {
        return classDeclNode::isClass;
    }

    private List<Symbol.MethodSymbol> initParentCtors() {
        List<Symbol.MethodSymbol> parentCtors = List.nil();
        Type.ClassType superClassType = classDeclNode.getSuperClassType();
        if (superClassType == null) return parentCtors;
        if (superClassType.tsym.getQualifiedName().equals(names().java_lang_Object)) return parentCtors;

        Symbol.ClassSymbol superClassSymbol = (Symbol.ClassSymbol) superClassType.tsym;
        Iterable<Symbol> symbols = superClassSymbol.members().getSymbols();
        for (Symbol symbol : symbols) {
            if (!(symbol instanceof Symbol.MethodSymbol methodSymbol)) continue;
            if (!methodSymbol.isConstructor()) continue;

            parentCtors = parentCtors.append(methodSymbol);
        }
        return parentCtors;
    }

    @Override
    public MethodDeclNode createCtorForFields(List<CodecEntryField> codecEntryFields) {
        MethodInvocationNode superCall = findMinArgParentConstructor();
        if (superCall != null) {
            // super() then assign fields
            return createPartialParentCallingCtor(superCall, codecEntryFields, codecEntryFields);
        }
        // assign fields
        return createFieldAssigningCtor(codecEntryFields);
    }

    private MethodDeclNode createFieldAssigningCtor(List<CodecEntryField> fields) {
        BlockNode.Builder bodyBuilder = BlockNode.builder(context);
        addFieldsToCtorBody(fields, bodyBuilder);
        BlockNode body = bodyBuilder.build();

        // public ClassName(fields) { this.field = field; ... }
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

        // private ClassName(parentArgs, fields) { super(parentArgs); this.field = field; ... }
        return MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(names().init)
                .params(fieldsToParameters(params))
                .body(body)
                .build();
    }

    private void addFieldsToCtorBody(List<CodecEntryField> codecEntryFields, BlockNode.Builder bodyBuilder) {
        for (CodecEntryField codecEntryField : codecEntryFields) {
            VariableDeclNode fieldNode = codecEntryField.field();
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
     * @return the invocation field for the parent constructor or {@code null} if no matching constructor is found.
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

    public List<VariableDeclNode> fieldsToParameters(List<CodecEntryField> codecEntryFields) {
        return codecEntryFields.map(field -> {
            VariableDeclNode fieldNode = field.field();
            FieldAdpater fieldAdpater = fieldAdapter(fieldNode.getType());
            return fieldAdpater.getParameter(fieldNode);
        });
    }
}
