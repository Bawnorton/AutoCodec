package com.bawnorton.autocodec.codec;

import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.node.parser.ExpressionParser;
import com.bawnorton.autocodec.util.ContextHolder;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;

public final class CodecReferenceCreator extends ContextHolder {
    private final ClassDeclNode classDecl;
    private final IncludedField includedField;
    private final VariableDeclNode field;
    
    public CodecReferenceCreator(ProcessingContext context, ClassDeclNode classDecl, IncludedField includedField) {
        super(context);
        this.classDecl = classDecl;
        this.includedField = includedField;
        this.field = includedField.fieldNode();
    }

    public CodecReference createReference() {
        MethodInvocationNode codecInvocation;
        if(includedField.isOptional()) {
            codecInvocation = optionalInvocation();
        } else {
            codecInvocation = requiredInvocation();
        }
        return new CodecReference(context, field, codecInvocation);
    }

    private MethodInvocationNode requiredInvocation() {
        VariableDeclNode field = includedField.fieldNode();

        // Codec.TYPE.fieldOf("fieldName")
        MethodInvocationNode fieldOfInvocation = createFieldOfInvocation(field);
        // forGetter(Class::fieldName)
        return getterInvocation(fieldOfInvocation);
    }

    private MethodInvocationNode optionalInvocation() {
        if(!includedField.hasOptionalValue()) {
            return optionalInvocationWithDefault();
        }

        String optionalValue = includedField.getOptionalValue();
        MethodDeclNode defaultMethod = tryFindMethodReference(optionalValue);
        if(defaultMethod == null) {
            return optionalInvocationFromExpression(parseOptionalValue(optionalValue));
        }
        return optionalInvocationFromMethodReference(defaultMethod);
    }

    /**
     * Given a java expression as a string, parse it into an {@link ExpressionNode}.
     * <br>For example, {@code "new ArrayList<>()"} would be parsed into a {@link NewClassNode}.
     */
    private ExpressionNode parseOptionalValue(String optionalValue) {
        return ExpressionParser.parseExpression(context, optionalValue);
    }

    private MethodDeclNode tryFindMethodReference(String optionalValue) {
        VariableDeclNode field = includedField.fieldNode();
        Type fieldType = field.getType();
        return classDecl.findMethod(optionalValue, fieldType);
    }

    private MethodInvocationNode optionalInvocationWithDefault() {
        return optionalInvocationFromExpression(LiteralNode.defaultLiteral(context, field.getType()));
    }

    private MethodInvocationNode optionalInvocationFromExpression(ExpressionNode expression) {
        return createOptionalInvocation(expression, null);
    }

    private MethodInvocationNode optionalInvocationFromMethodReference(MethodDeclNode defaultMethod) {
        return createOptionalInvocation(null, defaultMethod);
    }

    private MethodInvocationNode createOptionalInvocation(ExpressionNode expression, MethodDeclNode defaultMethod) {
        String optionalFieldName = field.getName() + "Optional";

        // Codec.TYPE.optionalFieldOf("fieldName")
        MethodInvocationNode optionalFieldOfInvocation = createOptionalFieldOfInvocation();
        // xmap
        FieldAccessNode xmapReference = createXmapReference(optionalFieldOfInvocation);
        // (Optional<FieldType> fieldNameOptional)
        VariableDeclNode fieldOptionalParam = createFieldOptionalParam(optionalFieldName);
        // fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        MethodInvocationNode orElseInvocation = createOrElseInvocation(optionalFieldName, expression, defaultMethod);
        // (fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        LambdaNode toLambda = createLambdaNode(fieldOptionalParam, orElseInvocation);
        // Optional::ofNullable
        MemberReferenceNode optionalOfNullableReference = createOptionalOfNullableReference();
        // xmap((Optional<FieldType> fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod), Optional::ofNullable)
        MethodInvocationNode xmapInvocation = createXmapInvocation(xmapReference, toLambda, optionalOfNullableReference);
        // forGetter(Class::fieldName)
        return getterInvocation(xmapInvocation);
    }

    private MethodInvocationNode createFieldOfInvocation(VariableDeclNode field) {
        FieldAccessNode fieldOfReference = createFieldOfReference(field);

        return MethodInvocationNode.builder(context)
                .methodSelect(fieldOfReference)
                .argument(LiteralNode.of(context, field.getName()))
                .build();
    }

    private MethodInvocationNode createOptionalFieldOfInvocation() {
        FieldAccessNode optionalFieldOfReference = FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, field.getBoxedType(types())))
                .name("optionalFieldOf")
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(optionalFieldOfReference)
                .argument(LiteralNode.of(context, field.getName()))
                .build();
    }

    private FieldAccessNode createFieldOfReference(VariableDeclNode field) {
        return FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, field.getBoxedType(types())))
                .name("fieldOf")
                .build();
    }

    private FieldAccessNode createXmapReference(MethodInvocationNode optionalFieldOfInvocation) {
        return FieldAccessNode.builder(context)
                .selected(optionalFieldOfInvocation)
                .name("xmap")
                .build();
    }

    private VariableDeclNode createFieldOptionalParam(String optionalFieldName) {
        return VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(optionalFieldName)
                .type("Optional")
                .genericParam(field.getBoxedType(types()))
                .noSym()
                .build();
    }

    private MethodInvocationNode createOrElseInvocation(String optionalFieldName, ExpressionNode expression, MethodDeclNode defaultMethod) {
        if (expression != null) {
            FieldAccessNode orElseReference = FieldAccessNode.builder(context)
                    .selected(optionalFieldName)
                    .name("orElse")
                    .build();

            return MethodInvocationNode.builder(context)
                    .methodSelect(orElseReference)
                    .argument(expression)
                    .build();
        } else {
            FieldAccessNode orElseGetReference = FieldAccessNode.builder(context)
                    .selected(optionalFieldName)
                    .name("orElseGet")
                    .build();

            MemberReferenceNode defaultMethodMemberReference = MemberReferenceNode.builder(context)
                    .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                    .name(defaultMethod.getName())
                    .expression(classDecl.getName())
                    .build();

            return MethodInvocationNode.builder(context)
                    .methodSelect(orElseGetReference)
                    .argument(defaultMethodMemberReference)
                    .build();
        }
    }

    private LambdaNode createLambdaNode(VariableDeclNode fieldOptionalParam, MethodInvocationNode orElseInvocation) {
        return LambdaNode.builder(context)
                .param(fieldOptionalParam)
                .body(orElseInvocation)
                .build();
    }

    private MemberReferenceNode createOptionalOfNullableReference() {
        return MemberReferenceNode.builder(context)
                .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                .name("ofNullable")
                .expression("Optional")
                .build();
    }

    private MethodInvocationNode createXmapInvocation(FieldAccessNode xmapReference, LambdaNode toLambda, MemberReferenceNode optionalOfNullableReference) {
        return MethodInvocationNode.builder(context)
                .methodSelect(xmapReference)
                .argument(toLambda)
                .argument(optionalOfNullableReference)
                .build();
    }

    private MethodInvocationNode getterInvocation(MethodInvocationNode supplier) {
        FieldAccessNode forGetterReference = FieldAccessNode.builder(context)
                .selected(supplier)
                .name("forGetter")
                .build();

        MemberReferenceNode selfFieldReference = MemberReferenceNode.builder(context)
                .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                .name(field.getName())
                .expression(classDecl.getName())
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(forGetterReference)
                .typeArgument(classDecl)
                .argument(selfFieldReference)
                .build();
    }
}
