package com.bawnorton.autocodec.codec.adapter.entry;

import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.node.parser.ExpressionParser;
import com.bawnorton.autocodec.util.Or;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;

public abstract class OptionalEntryAdapter extends EntryAdapter {
    protected final AnnotationNode optional;

    protected OptionalEntryAdapter(ProcessingContext context, AnnotationNode optional) {
        super(context);
        this.optional = optional;
    }

    @Override
    public CodecEntry createEntry(ClassDeclNode enclosingClass, VariableDeclNode field) {
        MethodInvocationNode optionalInvocation = optionalInvocation(enclosingClass, field);
        return new CodecEntry(field, optionalInvocation);
    }

    protected MethodInvocationNode optionalInvocation(ClassDeclNode enclosingClass, VariableDeclNode field) {
        if (!optional.hasValue()) {
            return optionalInvocationWithDefault(enclosingClass, field);
        }

        String optionalValue = optional.getValue(LiteralNode.class).getValue(String.class);
        MethodDeclNode defaultMethod = tryFindMethodReference(optionalValue, enclosingClass, field);
        if(defaultMethod == null) {
            return optionalInvocationFromExpression(parseOptionalValue(optionalValue), enclosingClass, field);
        }
        return optionalInvocationFromMethodReference(defaultMethod, enclosingClass, field);
    }

    /**
     * Given a java expression as a string, parse it into an {@link ExpressionNode}.
     * <br>For example, {@code "new ArrayList<>()"} would be parsed into a {@link NewClassNode}.
     */
    private ExpressionNode parseOptionalValue(String optionalValue) {
        return ExpressionParser.parseExpression(context, optionalValue);
    }

    private MethodDeclNode tryFindMethodReference(String optionalValue, ClassDeclNode enclosingClass, VariableDeclNode field) {
        Type fieldType = field.getType();
        return enclosingClass.findMethod(optionalValue, fieldType);
    }

    private MethodInvocationNode optionalInvocationWithDefault(ClassDeclNode enclosingClass, VariableDeclNode field) {
        return optionalInvocationFromExpression(LiteralNode.defaultLiteral(context, field.getType()), enclosingClass, field);
    }

    private MethodInvocationNode optionalInvocationFromExpression(ExpressionNode expression, ClassDeclNode enclosingClass, VariableDeclNode field) {
        MethodInvocationNode optionalInvocation = createOptionalInvocation(field);
        return createXmapper(optionalInvocation, enclosingClass, field, Or.left(expression));
    }

    private MethodInvocationNode optionalInvocationFromMethodReference(MethodDeclNode defaultMethod, ClassDeclNode enclosingClass, VariableDeclNode field) {
        MethodInvocationNode optionalInvocation = createOptionalInvocation(field);
        return createXmapper(optionalInvocation, enclosingClass, field, Or.right(defaultMethod));
    }

    protected abstract MethodInvocationNode createOptionalInvocation(VariableDeclNode field);

    protected MethodInvocationNode createXmapper(MethodInvocationNode optionalFieldOfInvocation, ClassDeclNode enclosingClass, VariableDeclNode field, Or<ExpressionNode, MethodDeclNode> refOrMethod) {
        String optionalFieldName = field.getName() + "Optional";
        // xmap
        FieldAccessNode xmapReference = createXmapReference(optionalFieldOfInvocation);
        // (Optional<FieldType> fieldNameOptional)
        VariableDeclNode fieldOptionalParam = createFieldOptionalParam(optionalFieldName, field);
        // fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        MethodInvocationNode orElseInvocation = createOrElseInvocation(optionalFieldName, enclosingClass, refOrMethod);
        // (fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod)
        LambdaNode toLambda = createLambdaNode(fieldOptionalParam, orElseInvocation);
        // Optional::ofNullable
        MemberReferenceNode optionalOfNullableReference = createOptionalOfNullableReference();
        // xmap((Optional<FieldType> fieldNameOptional) -> fieldNameOptional.orElse(expression) / fieldNameOptional.orElseGet(defaultMethod), Optional::ofNullable)
        MethodInvocationNode xmapInvocation = createXmapInvocation(xmapReference, toLambda, optionalOfNullableReference);
        // forGetter(Class::fieldName)
        return getterInvocation(xmapInvocation, enclosingClass, field);
    }

    protected MethodInvocationNode createOptionalFieldOfInvocation(ExpressionNode codecExpression, VariableDeclNode field) {
        FieldAccessNode optionalFieldOfReference = FieldAccessNode.builder(context)
                .selected(codecExpression)
                .name("optionalFieldOf")
                .build();

        return MethodInvocationNode.builder(context)
                .methodSelect(optionalFieldOfReference)
                .argument(LiteralNode.of(context, field.getName()))
                .build();
    }

    protected FieldAccessNode createXmapReference(MethodInvocationNode optionalFieldOfInvocation) {
        return FieldAccessNode.builder(context)
                .selected(optionalFieldOfInvocation)
                .name("xmap")
                .build();
    }

    protected VariableDeclNode createFieldOptionalParam(String optionalFieldName, VariableDeclNode field) {
        VariableDeclNode.Builder fieldBuilder = VariableDeclNode.builder(context)
                .modifiers(Flags.PARAMETER)
                .name(optionalFieldName)
                .type("Optional");
        if (field.isPrimitive()) {
            fieldBuilder.genericParam(field.getBoxedType(types()));
        } else {
            fieldBuilder.genericParam(field.getVartype());
        }
        return fieldBuilder.build();
    }

    protected MethodInvocationNode createOrElseInvocation(String optionalFieldName, ClassDeclNode enclosingClass, Or<ExpressionNode, MethodDeclNode> refOrMethod) {
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

    protected LambdaNode createLambdaNode(VariableDeclNode fieldOptionalParam, MethodInvocationNode orElseInvocation) {
        return LambdaNode.builder(context)
                .param(fieldOptionalParam)
                .body(orElseInvocation)
                .build();
    }

    protected MemberReferenceNode createOptionalOfNullableReference() {
        return MemberReferenceNode.builder(context)
                .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                .name("ofNullable")
                .expression("Optional")
                .build();
    }

    protected MethodInvocationNode createXmapInvocation(FieldAccessNode xmapReference, LambdaNode toLambda, MemberReferenceNode optionalOfNullableReference) {
        return MethodInvocationNode.builder(context)
                .methodSelect(xmapReference)
                .argument(toLambda)
                .argument(optionalOfNullableReference)
                .build();
    }
}
