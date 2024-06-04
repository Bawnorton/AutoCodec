package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;
import com.bawnorton.autocodec.nodes.*;
import com.bawnorton.autocodec.util.CodecLookup;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CodecFieldAdder extends NodeVisitor {
    private static final String CODEC_IMPORT = "com.mojang.serialization.Codec";
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
        if(hasCodecImport(compilationUnitNode)) return;

        compilationUnitNode.addImport(createCodecImport());
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

    private boolean hasCodecImport(CompilationUnitNode compilationUnitNode) {
        List<ImportNode> imports = compilationUnitNode.getImports();
        for (ImportNode importNode : imports) {
            String importPath = importNode.getImportPath();
            if (!importPath.equals(CODEC_IMPORT)) continue;

            return true;
        }
        return false;
    }

    private VariableDeclNode createCodecField(ClassDeclNode classDeclNode) {
        List<VariableDeclNode> fields = classDeclNode.getFields();
        List<VariableDeclNode> included = new ArrayList<>();
        List<VariableDeclNode> optional = new ArrayList<>();

        for (VariableDeclNode field : fields) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            if (field.annotationPresent(Optional.class)) {
                optional.add(field);
            } else {
                included.add(field);
            }
        }

        Map<VariableDeclNode, MethodInvocationNode> includedCodecs = new HashMap<>();
        for (VariableDeclNode includedField : included) {
            FieldAccessNode fieldOfReferenceNode = FieldAccessNode.builder(holder.getContext())
                    .selected(CodecLookup.lookupCodec(holder.getContext(), includedField.getType()))
                    .name("fieldOf")
                    .build();

            LiteralNode literalFieldNameNode = LiteralNode.builder(holder.getContext())
                    .value(includedField.getName())
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
                    .name(includedField.getName())
                    .expression("instance")
                    .build();

            MethodInvocationNode forGetterInvocationNode = MethodInvocationNode.builder(holder.getContext())
                    .methodSelect(forGetterReferenceNode)
                    .argument(selfFieldReferenceNode)
                    .build();
        }

        FieldAccessNode createReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("RecordCodecBuilder")
                .name("create")
                .build();

        FieldAccessNode implicitGroupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("instance")
                .name("group")
                .build();


        MethodInvocationNode groupContentInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(implicitGroupReferenceNode)
                .build();

        FieldAccessNode groupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("RecordCodecBuilder")
                .name("group")
                .build();

        MethodInvocationNode groupInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .build();

        VariableDeclNode instanceParamNode = VariableDeclNode.builder(holder.getContext())
                .name("instance")
                .implicitType()
                .build();

        LambdaNode lambdaNode = LambdaNode.builder(holder.getContext())
                .param(instanceParamNode)
//                .body()
                .build();

        MethodInvocationNode invocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(createReferenceNode)
//                .arguments()
                .build();

        return VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .name(codecFieldName)
                .type("Codec")
                .genericParam(classDeclNode.getTree().name)
                .initialValue(null)
                .build();
    }

    private ImportNode createCodecImport() {
        return ImportNode.builder(holder.getContext())
                .importPath(CODEC_IMPORT)
                .isStatic(false)
                .build();
    }
}
