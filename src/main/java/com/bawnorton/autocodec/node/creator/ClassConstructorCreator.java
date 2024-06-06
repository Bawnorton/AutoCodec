package com.bawnorton.autocodec.node.creator;

import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class ClassConstructorCreator {
    private final ClassDeclNode classDeclNode;
    private final List<Symbol.MethodSymbol> parentCtors;

    public ClassConstructorCreator(ClassDeclNode classDeclNode) {
        if(!classDeclNode.isClass()) {
            throw new IllegalArgumentException("%s is not a `class`.".formatted(classDeclNode.getName()));
        }

        this.classDeclNode = classDeclNode;
        this.parentCtors = initParentCtors();
    }

    private List<Symbol.MethodSymbol> initParentCtors() {
        List<Symbol.MethodSymbol> parentCtors = new ArrayList<>();
        Type superClassType = classDeclNode.getSuperClassType();
        if (superClassType == null) return parentCtors;

        Symbol.ClassSymbol superClassSymbol = (Symbol.ClassSymbol) superClassType.tsym;
        Iterable<Symbol> symbols = superClassSymbol.members().getSymbols();
        for (Symbol symbol : symbols) {
            if (!(symbol instanceof Symbol.MethodSymbol methodSymbol)) continue;
            if (!methodSymbol.isConstructor()) continue;

            parentCtors.add(methodSymbol);
        }
        return parentCtors;
    }

    public void createCtorForFields(ProcessingContext context, List<IncludedField> includedFields) {
        MethodInvocationNode superCall = findIdenticalParentCtor(context, includedFields);
        if (superCall != null) {
            createParentCallingCtor(context, superCall, includedFields);
            return;
        }

        // find a parent Ctor that references some of the fields as parameters and keep track of the matched fields.
        List<IncludedField> unmatchedFields = new ArrayList<>(includedFields);
        superCall = findPartialParentCtor(context, includedFields, unmatchedFields::remove);
        if (superCall != null) {
            createPartialParentCallingCtor(context, superCall, includedFields, unmatchedFields);
            return;
        }

        createFieldAssigningCtor(context, includedFields);
    }

    private void createFieldAssigningCtor(ProcessingContext context, List<IncludedField> includedFields) {
        BlockNode.Builder bodyBuilder = BlockNode.builder(context);
        addFieldsToCtorBody(context, includedFields, bodyBuilder);
        BlockNode body = bodyBuilder.build();

        // public ClassName(fields) { this.field = field; ... }
        classDeclNode.addMethod(MethodDeclNode.builder(context)
                .modifiers(Flags.PUBLIC)
                .name(context.names().init)
                .params(fieldsToParameters(context, includedFields))
                .body(body)
                .build());
    }

    private void createPartialParentCallingCtor(ProcessingContext context, MethodInvocationNode superCall, List<IncludedField> includedFields, List<IncludedField> unmatchedFields) {
        // super(args)
        BlockNode.Builder bodyBuilder = BlockNode.builder(context)
                .statement(superCall.toStatement(context.treeMaker()));
        addFieldsToCtorBody(context, unmatchedFields, bodyBuilder);
        BlockNode body = bodyBuilder.build();

        // private ClassName(parentArgs, fields) { super(parentArgs); this.field = field; ... }
        classDeclNode.addMethod(MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(context.names().init)
                .params(fieldsToParameters(context, includedFields))
                .body(body)
                .build());
    }

    private void createParentCallingCtor(ProcessingContext context, MethodInvocationNode superCall, List<IncludedField> includedFields) {
        // super(args)
        BlockNode body = BlockNode.builder(context)
                .statement(superCall.toStatement(context.treeMaker()))
                .build();

        // private ClassName(args) { super(args); }
        classDeclNode.addMethod(MethodDeclNode.builder(context)
                .modifiers(Flags.PRIVATE)
                .name(context.names().init)
                .params(fieldsToParameters(context, includedFields))
                .body(body)
                .build());
    }

    private void addFieldsToCtorBody(ProcessingContext context, List<IncludedField> includedFields, BlockNode.Builder bodyBuilder) {
        for (IncludedField includedField : includedFields) {
            VariableDeclNode field = includedField.variableDeclNode();

            // this.field = field
            FieldAccessNode assignmentNode = FieldAccessNode.builder(context)
                    .selected(IdentNode.of(context, "this"))
                    .name(field.getName())
                    .build();

            bodyBuilder.statement(AssignNode.builder(context)
                    .lhs(assignmentNode)
                    .rhs(field.getName())
                    .build()
                    .toStatement(context.treeMaker()));
        }
    }

    /**
     * looks for a parent constructor that matches the fields exactly.
     * <br>eg:
     * <pre>
     *     {@code
     *     public class Child extends Parent {
     *          private String s1;
     *          private String s2;
     *     }
     *
     *     public class Parent {
     *          private String s1;
     *          private String s2;
     *
     *          public Parent(String s1, String s2) {
     *              this.s1 = s1;
     *              this.s2 = s2;
     *         }
     *    }
     *    }
     * </pre>
     * creates the invocation {@code super(s1, s2)} for the {@code Child} constructor.
     *
     * @return the invocation node for the parent constructor or {@code null} if no matching constructor is found.
     */
    private MethodInvocationNode findIdenticalParentCtor(ProcessingContext context, List<IncludedField> includedFields) {
        for (Symbol.MethodSymbol parentCtor : parentCtors) {
            List<Symbol.VarSymbol> parameters = parentCtor.getParameters();
            if (parameters.size() != includedFields.size()) continue;

            boolean foundValidParentCtor = true;
            for (int i = 0; i < parameters.size(); i++) {
                Symbol.VarSymbol parameter = parameters.get(i);
                IncludedField includedField = includedFields.get(i);
                VariableDeclNode field = includedField.variableDeclNode();

                if (!field.sameNameAndType(parameter)) {
                    foundValidParentCtor = false;
                    break;
                }
            }

            if (!foundValidParentCtor) continue;

            List<IdentNode> parameterNodes = new ArrayList<>();
            for (Symbol.VarSymbol parameter : parameters) {
                parameterNodes.add(IdentNode.of(context, parameter.name));
            }

            return MethodInvocationNode.builder(context)
                    .methodSelect("super")
                    .arguments(parameterNodes)
                    .build();
        }
        return null;
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
     * if a {@code Parent} field is of type {@code int} then instead of passing {@code null} as the argument, {@code 0} is passed.
     *
     * @param onMatch a consumer that is called when a field is matched to a parameter, this is used to separate the matched fields from the unmatched fields.
     * @return the invocation node for the parent constructor or {@code null} if no matching constructor is found.
     */
    private MethodInvocationNode findPartialParentCtor(ProcessingContext context, List<IncludedField> includedFields, Consumer<IncludedField> onMatch) {
        for (Symbol.MethodSymbol parentCtor : parentCtors) {
            List<Symbol.VarSymbol> parameters = parentCtor.getParameters();
            List<ExpressionNode> arguments = new ArrayList<>();
            for (Symbol.VarSymbol parameter : parameters) {
                boolean found = false;
                List<IncludedField> accountedFields = new ArrayList<>();
                for (IncludedField includedField : includedFields) {
                    VariableDeclNode field = includedField.variableDeclNode();

                    if (field.sameNameAndType(parameter)) {
                        arguments.add(IdentNode.of(context, field.getName()));
                        accountedFields.add(includedField);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    arguments.add(LiteralNode.defaultLiteral(context, parameter.type));
                }
                accountedFields.forEach(onMatch);
            }

            return MethodInvocationNode.builder(context)
                    .methodSelect("super")
                    .arguments(arguments)
                    .build();
        }
        return null;
    }


    private List<VariableDeclNode> fieldsToParameters(ProcessingContext context, List<IncludedField> matchedFields) {
        List<VariableDeclNode> parameters = new ArrayList<>();
        for (IncludedField matched : matchedFields) {
            VariableDeclNode field = matched.variableDeclNode();
            VariableDeclNode parameter = VariableDeclNode.builder(context)
                    .modifiers(Flags.PARAMETER)
                    .name(field.getName())
                    .type(field.getType().tsym.name)
                    .build();
            parameters.add(parameter);
        }
        return parameters;
    }
}
