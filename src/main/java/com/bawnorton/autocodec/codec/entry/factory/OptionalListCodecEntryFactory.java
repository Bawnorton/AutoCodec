package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.util.Or;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;

public final class OptionalListCodecEntryFactory extends OptionalCodecEntryFactory {
    public OptionalListCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode, AnnotationNode optionalAnnotation) {
        super(context, enclosingClass, fieldNode, optionalAnnotation);
    }

    protected MethodInvocationNode createOptionalInvocation(Or<ExpressionNode, MethodDeclNode> refOrMethod) {
        String optionalFieldName = fieldNode.getName() + "Optional";

        // Codec.TYPE.optionalFieldOf("fieldName")
        MethodInvocationNode optionalFieldOfInvocation = createOptionalFieldOfInvocation();
        // xmap
        FieldAccessNode xmapReference = createXmapReference(optionalFieldOfInvocation);
        // (Optional<FieldType> fieldNameOptional)
        VariableDeclNode fieldOptionalParam = createFieldOptionalParam(optionalFieldName);
        // fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        MethodInvocationNode orElseInvocation = createOrElseInvocation(optionalFieldName, refOrMethod);
        // (fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        LambdaNode toLambda = createLambdaNode(fieldOptionalParam, orElseInvocation);
        // Optional::ofNullable
        MemberReferenceNode optionalOfNullableReference = createOptionalOfNullableReference();
        // xmap((Optional<FieldType> fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod), Optional::ofNullable)
        MethodInvocationNode xmapInvocation = createXmapInvocation(xmapReference, toLambda, optionalOfNullableReference);
        // forGetter(Class::fieldName)
        return getterInvocation(xmapInvocation);
    }

    private MethodInvocationNode createOptionalFieldOfInvocation() {
        FieldAccessNode optionalFieldOfReference = FieldAccessNode.builder(context)
                .selected(CodecLookup.lookupCodec(context, fieldNode.getBoxedType(types())))
                .name("optionalFieldOf")
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(optionalFieldOfReference)
                .argument(LiteralNode.of(context, fieldNode.getName()))
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
                .genericParam(fieldNode.getBoxedType(types()))
                .noSym()
                .build();
    }

    private MethodInvocationNode createOrElseInvocation(String optionalFieldName, Or<ExpressionNode, MethodDeclNode> refOrMethod) {
        if (refOrMethod.isLeft()) {
            FieldAccessNode orElseReference = FieldAccessNode.builder(context)
                    .selected(optionalFieldName)
                    .name("orElse")
                    .build();

            return MethodInvocationNode.builder(context)
                    .methodSelect(orElseReference)
                    .argument(refOrMethod.left())
                    .build();
        } else if (refOrMethod.isRight()) {
            FieldAccessNode orElseGetReference = FieldAccessNode.builder(context)
                    .selected(optionalFieldName)
                    .name("orElseGet")
                    .build();

            MemberReferenceNode defaultMethodMemberReference = MemberReferenceNode.builder(context)
                    .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                    .name(refOrMethod.right().getName())
                    .expression(enclosingClass.getName())
                    .build();

            return MethodInvocationNode.builder(context)
                    .methodSelect(orElseGetReference)
                    .argument(defaultMethodMemberReference)
                    .build();
        } else {
            throw new IllegalStateException("Either a default value or a method reference must be present");
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
}
