package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;
import com.bawnorton.autocodec.codec.CodecReference;
import com.bawnorton.autocodec.info.RecordInfo;
import com.bawnorton.autocodec.nodes.*;
import com.bawnorton.autocodec.codec.CodecLookup;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodecFieldAdder extends NodeVisitor {
    private static final int MAX_FIELDS = 16;
    private static final List<String> NEEDED_IMPORTS = new ArrayList<>();

    static {
        NEEDED_IMPORTS.add("com.mojang.serialization.Codec");
        NEEDED_IMPORTS.add("com.mojang.serialization.codecs.RecordCodecBuilder");
    }

    private final String codecFieldName;

    public CodecFieldAdder(ProcessingContext context, String codecFieldName) {
        super(context);
        this.codecFieldName = codecFieldName;
    }

    @Override
    public void visitClassDeclNode(ClassDeclNode classDeclNode) {
        if(cantAddCodecField(classDeclNode)) return;

        classDeclNode.addField(createCodecField(classDeclNode));
    }

    @Override
    protected void visitCompilationUnitNode(CompilationUnitNode compilationUnitNode) {
        for (String importPath : NEEDED_IMPORTS) {
            if (missingImport(compilationUnitNode, importPath)) {
                compilationUnitNode.addImport(createImport(importPath));
            }
        }
    }

    private boolean missingImport(CompilationUnitNode compilationUnitNode, String importPath) {
        List<ImportNode> imports = compilationUnitNode.getImports();
        for (ImportNode importNode : imports) {
            if (importNode.getImportPath().equals(importPath)) {
                return false;
            }
        }
        return true;
    }

    private ImportNode createImport(String importPath) {
        return ImportNode.builder(holder.getContext())
                .importPath(importPath)
                .isStatic(false)
                .build();
    }

    private boolean cantAddCodecField(ClassDeclNode classDeclNode) {
        List<VariableDeclNode> fields = classDeclNode.getFields();
        if(fields.isEmpty()) return true;
        if(fields.stream().filter(field -> {
            boolean ignore = field.annotationPresent(Ignore.class);
            boolean isStatic = field.isStatic();
            return !(ignore || isStatic);
        }).count() > MAX_FIELDS) {
            return true;
        }

        for (VariableDeclNode field : fields) {
            if (!field.getName().equals(codecFieldName)) continue;

            Type type = field.getType();
            if (type == null) continue;

            if (type.toString().startsWith("com.mojang.serialization.Codec")) {
                return true;
            }
        }
        return false;
    }

    private VariableDeclNode createCodecField(ClassDeclNode classDeclNode) {
        List<CodecReference> includedCodecs = new ArrayList<>();
        List<IncludedField> includedFields = getIncludedFields(classDeclNode);
        findOrCreateConstructor(classDeclNode, includedFields);

        for (IncludedField includedField : includedFields) {
            VariableDeclNode field = includedField.variableDeclNode();
            boolean isRequired = !includedField.optional();

            FieldAccessNode fieldOfReferenceNode = FieldAccessNode.builder(holder.getContext())
                    .selected(CodecLookup.lookupCodec(holder.getContext(), field.getType()))
                    .name("fieldOf")
                    .build();

            LiteralNode literalFieldNameNode = LiteralNode.builder(holder.getContext())
                    .value(field.getName())
                    .build();

            MethodInvocationNode fieldOfInvocationNode = MethodInvocationNode.builder(holder.getContext())
                    .methodSelect(fieldOfReferenceNode)
                    .typeArgument((Type.ClassType) field.getType())
                    .argument(literalFieldNameNode)
                    .build();

            FieldAccessNode forGetterReferenceNode = FieldAccessNode.builder(holder.getContext())
                    .selected(fieldOfInvocationNode)
                    .name("forGetter")
                    .build();

            MemberReferenceNode selfFieldReferenceNode = MemberReferenceNode.builder(holder.getContext())
                    .mode(MemberReferenceTree.ReferenceMode.INVOKE)
                    .name(field.getName())
                    .expression(classDeclNode.getName())
                    .build();

            MethodInvocationNode forGetterInvocationNode = MethodInvocationNode.builder(holder.getContext())
                    .methodSelect(forGetterReferenceNode)
                    .typeArgument(classDeclNode)
                    .argument(selfFieldReferenceNode)
                    .build();

            includedCodecs.add(new CodecReference(field, forGetterInvocationNode));
        }

        FieldAccessNode instanceGroupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("instance")
                .name("group")
                .build();

        MethodInvocationNode.Builder instanceGroupInvocationNodeBuilder = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(instanceGroupReferenceNode);
        for (CodecReference reference : includedCodecs) {
            instanceGroupInvocationNodeBuilder.typeArgument(reference.getType());
            instanceGroupInvocationNodeBuilder.argument(reference.codecNode());
        }
        MethodInvocationNode instanceGroupInvocationNode = instanceGroupInvocationNodeBuilder.build();

        FieldAccessNode applyReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected(instanceGroupInvocationNode)
                .name("apply")
                .build();

        MemberReferenceNode selfConstructorReferenceNode = MemberReferenceNode.builder(holder.getContext())
                .mode(MemberReferenceTree.ReferenceMode.NEW)
                .name("<init>")
                .expression(classDeclNode.getName())
                .build();

        MethodInvocationNode applyInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(applyReferenceNode)
                .typeArgument(classDeclNode)
                .argument("instance")
                .argument(selfConstructorReferenceNode)
                .build();

        VariableDeclNode instanceReferenceNode = VariableDeclNode.builder(holder.getContext())
                .name("instance")
                .enclosingType("RecordCodecBuilder")
                .type("Instance")
                .genericParam(classDeclNode)
                .noSym()
                .build();

        LambdaNode instanceGroupLambdaNode = LambdaNode.builder(holder.getContext())
                .param(instanceReferenceNode)
                .body(applyInvocationNode)
                .build();

        FieldAccessNode recordCodecBuilderCreateReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("RecordCodecBuilder")
                .name("create")
                .build();

        MethodInvocationNode recordCodecBuilderCreateInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(recordCodecBuilderCreateReferenceNode)
                .typeArgument(classDeclNode)
                .argument(instanceGroupLambdaNode)
                .build();

        VariableDeclNode codecNode = VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL)
                .name(codecFieldName)
                .owner(classDeclNode)
                .module("com.mojang.serialization")
                .type("Codec")
                .genericParam(classDeclNode)
                .initialValue(recordCodecBuilderCreateInvocationNode)
                .build();
        holder.printMessage("Added codec field to class " + classDeclNode.getName() + ": " + codecNode);
        return codecNode;
    }

    private void ensureFieldOrder(MethodDeclNode constructor, List<IncludedField> includedFields) {
        List<VariableDeclNode> parameters = constructor.getParameters();
        List<IncludedField> reordered = new ArrayList<>();
        for (VariableDeclNode parameter : parameters) {
            for (IncludedField includedField : includedFields) {
                VariableDeclNode field = includedField.variableDeclNode();
                String fieldName = field.getName();
                String parameterName = parameter.getName();

                Type fieldType = field.getType();
                Type parameterType = parameter.getType();
                if (fieldName.equals(parameterName) && fieldType.equals(parameterType)) {
                    reordered.add(includedField);
                    break;
                }
            }
        }
        includedFields.clear();
        includedFields.addAll(reordered);
    }

    private void findOrCreateConstructor(ClassDeclNode classDeclNode, List<IncludedField> includedFields) {
        MethodDeclNode constructor = findConstructor(classDeclNode, includedFields);
        if(classDeclNode.isRecord()) {
            if(constructor == null) {
                RecordInfo recordInfo = new RecordInfo(classDeclNode);
                constructor = createRecordConstructor(recordInfo, includedFields);
                classDeclNode.addMethod(constructor);
            } else {
                ensureFieldOrder(constructor, includedFields);
            }
        }
    }

    private MethodDeclNode findConstructor(ClassDeclNode classDeclNode, List<IncludedField> includedFields) {
        List<MethodDeclNode> constructors = classDeclNode.getConstructors();
        for (MethodDeclNode constructor : constructors) {
            List<VariableDeclNode> parameters = constructor.getParameters();
            if (parameters.size() != includedFields.size()) continue;

            boolean allMatch = true;
            for (int i = 0; i < parameters.size(); i++) {
                VariableDeclNode parameter = parameters.get(i);
                IncludedField includedField = includedFields.get(i);
                Type parameterType = parameter.getType();
                Type fieldType = includedField.variableDeclNode().getType();
                if (!parameterType.equals(fieldType)) {
                    allMatch = false;
                    break;
                }
            }

            if (allMatch) return constructor;
        }
        return null;
    }


    private MethodDeclNode createRecordConstructor(RecordInfo recordInfo, List<IncludedField> includedFields) {
        MethodDeclNode mainConstructor = recordInfo.getMainConstructor();
        ClassDeclNode classDeclNode = recordInfo.getNode();

        List<VariableDeclNode> parameters = new ArrayList<>();
        for (IncludedField includedField : includedFields) {
            VariableDeclNode field = includedField.variableDeclNode();
            VariableDeclNode parameter = VariableDeclNode.builder(holder.getContext())
                    .modifiers(Flags.PARAMETER)
                    .name(field.getName())
                    .owner(classDeclNode)
                    .type(field.getType().tsym.name)
                    .build();
            parameters.add(parameter);
        }

        List<ExpressionNode> thisCallArguments = new ArrayList<>();
        for (VariableDeclNode parameter : mainConstructor.getParameters()) {
            List<AnnotationNode> annotations = recordInfo.getAssociatedFieldAnnotations(parameter);
            boolean ignore = annotations.stream().anyMatch(annotation -> annotation.isOfType(Ignore.class));
            if (ignore) {
                thisCallArguments.add(LiteralNode.nullLiteral(holder.getContext()));
            } else {
                thisCallArguments.add(IdentNode.of(holder.getContext(), parameter.getName()));
            }
        }

        MethodInvocationNode thisCallInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect("this")
                .arguments(thisCallArguments)
                .build();

        BlockNode newConstructorBody = BlockNode.builder(holder.getContext())
                .statement(thisCallInvocationNode.toStatement(holder.treeMaker()))
                .build();

        return MethodDeclNode.builder(holder.getContext())
                .modifiers(Flags.PRIVATE)
                .name(holder.names().init)
                .params(parameters)
                .body(newConstructorBody)
                .build();
    }

    private List<IncludedField> getIncludedFields(ClassDeclNode classDeclNode) {
        List<VariableDeclNode> fields = classDeclNode.getFields();
        List<IncludedField> included = new ArrayList<>();

        for (VariableDeclNode field : fields) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            included.add(new IncludedField(field, field.annotationPresent(Optional.class)));
        }

        return included;
    }
}
