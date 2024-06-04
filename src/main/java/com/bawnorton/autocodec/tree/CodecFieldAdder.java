package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;
import com.bawnorton.autocodec.nodes.*;
import com.bawnorton.autocodec.util.CodecLookup;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Pair;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecFieldAdder extends NodeVisitor {
    private static final String CODEC_IMPORT = "com.mojang.serialization.Codec";
    private static final String RECORD_CODEC_BUILDER_IMPORT = "com.mojang.serialization.codecs.RecordCodecBuilder";
    private static final int MAX_FIELDS = 16;

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
        if(missingCodecImport(compilationUnitNode)) {
            compilationUnitNode.addImport(createCodecImport());
        }
        if(missingRecordCodecBuilderImport(compilationUnitNode)) {
            compilationUnitNode.addImport(createRecordCodecBuilderImport());
        }
    }

    private boolean missingCodecImport(CompilationUnitNode compilationUnitNode) {
        List<ImportNode> imports = compilationUnitNode.getImports();
        for (ImportNode importNode : imports) {
            String importPath = importNode.getImportPath();
            if (!importPath.equals(CODEC_IMPORT)) continue;

            return false;
        }
        return true;
    }

    private ImportNode createCodecImport() {
        return ImportNode.builder(holder.getContext())
                .importPath(CODEC_IMPORT)
                .isStatic(false)
                .build();
    }

    private boolean missingRecordCodecBuilderImport(CompilationUnitNode compilationUnitNode) {
        List<ImportNode> imports = compilationUnitNode.getImports();
        for (ImportNode importNode : imports) {
            String importPath = importNode.getImportPath();
            if (!importPath.equals(RECORD_CODEC_BUILDER_IMPORT)) continue;

            return false;
        }
        return true;
    }

    private ImportNode createRecordCodecBuilderImport() {
        return ImportNode.builder(holder.getContext())
                .importPath(RECORD_CODEC_BUILDER_IMPORT)
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

            if (type.toString().startsWith(CODEC_IMPORT)) {
                return true;
            }
        }
        return false;
    }

    private VariableDeclNode createCodecField(ClassDeclNode classDeclNode) {
        List<VariableDeclNode> fields = classDeclNode.getFields();
        List<Pair<VariableDeclNode, Boolean>> included = new ArrayList<>();

        for (VariableDeclNode field : fields) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            if (field.annotationPresent(Optional.class)) {
                included.add(Pair.of(field, false));
            } else {
                included.add(Pair.of(field, true));
            }
        }

        Map<VariableDeclNode, MethodInvocationNode> includedCodecs = new HashMap<>();
        for (Pair<VariableDeclNode, Boolean> includedField : included) {
            VariableDeclNode field = includedField.fst;
            boolean isRequired = includedField.snd;

            FieldAccessNode fieldOfReferenceNode = FieldAccessNode.builder(holder.getContext())
                    .selected(CodecLookup.lookupCodec(holder.getContext(), field.getType()))
                    .name("fieldOf")
                    .build();

            LiteralNode literalFieldNameNode = LiteralNode.builder(holder.getContext())
                    .value(field.getName())
                    .build();

            MethodInvocationNode fieldOfInvocationNode = MethodInvocationNode.builder(holder.getContext())
                    .methodSelect(fieldOfReferenceNode)
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
                    .argument(selfFieldReferenceNode)
                    .build();

            includedCodecs.put(field, forGetterInvocationNode);
        }

        FieldAccessNode instanceGroupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("instance")
                .name("group")
                .build();

        MethodInvocationNode.Builder instanceGroupInvocationNodeBuilder = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(instanceGroupReferenceNode);
        includedCodecs.values().forEach(instanceGroupInvocationNodeBuilder::argument);
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
                .argument("instance")
                .argument(selfConstructorReferenceNode)
                .build();

        VariableDeclNode instanceReferenceNode = VariableDeclNode.builder(holder.getContext())
                .implicitType()
                .name("instance")
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
                .argument(instanceGroupLambdaNode)
                .build();

        VariableDeclNode codecNode = VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .name(codecFieldName)
                .type("Codec")
                .genericParam(classDeclNode.getName())
                .initialValue(recordCodecBuilderCreateInvocationNode)
                .build();
        holder.printMessage("Created codec field: " + codecNode);
        return codecNode;
    }
}
