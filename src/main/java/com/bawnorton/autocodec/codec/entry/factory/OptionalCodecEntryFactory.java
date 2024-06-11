package com.bawnorton.autocodec.codec.entry.factory;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.node.parser.ExpressionParser;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.util.Or;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;

public abstract class OptionalCodecEntryFactory extends CodecEntryFactory {
    protected final AnnotationNode optionalAnnotation;

    protected OptionalCodecEntryFactory(ProcessingContext context, ClassDeclNode enclosingClass, VariableDeclNode fieldNode, AnnotationNode optionalAnnotation) {
        super(context, enclosingClass, fieldNode);
        this.optionalAnnotation = optionalAnnotation;
    }

    @Override
    public CodecEntry createEntry() {
        MethodInvocationNode optionalInvocation = optionalInvocation();
        return new CodecEntry(fieldNode, optionalInvocation);
    }

    protected MethodInvocationNode optionalInvocation() {
        if(!optionalAnnotation.hasValue()) {
            return optionalInvocationWithDefault();
        }

        String optionalValue = optionalAnnotation.getValue(LiteralNode.class).getValue(String.class);
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
        Type fieldType = fieldNode.getType();
        return enclosingClass.findMethod(optionalValue, fieldType);
    }

    private MethodInvocationNode optionalInvocationWithDefault() {
        return optionalInvocationFromExpression(LiteralNode.defaultLiteral(context, fieldNode.getType()));
    }

    private MethodInvocationNode optionalInvocationFromExpression(ExpressionNode expression) {
        return createOptionalInvocation(Or.left(expression));
    }

    private MethodInvocationNode optionalInvocationFromMethodReference(MethodDeclNode defaultMethod) {
        return createOptionalInvocation(Or.right(defaultMethod));
    }

    protected abstract MethodInvocationNode createOptionalInvocation(Or<ExpressionNode, MethodDeclNode> refOrMethod);

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
