package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.OptionalEntry;
import com.bawnorton.autocodec.codec.CodecReferenceCreator;
import com.bawnorton.autocodec.codec.CodecReference;
import com.bawnorton.autocodec.node.*;
import com.bawnorton.autocodec.node.creator.ConstructorCreator;
import com.bawnorton.autocodec.util.IncludedField;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;

public class CodecAdder extends NodeVisitor {
    private static final int MAX_FIELDS = 16;
    private static final List<String> NEEDED_IMPORTS = new ArrayList<>();

    static {
        NEEDED_IMPORTS.add("com.mojang.serialization.Codec");
        NEEDED_IMPORTS.add("com.mojang.serialization.codecs.RecordCodecBuilder");
    }

    private final String codecFieldName;

    public CodecAdder(ProcessingContext context, String codecFieldName) {
        super(context);
        this.codecFieldName = codecFieldName;
    }

    @Override
    public void visitClassDeclNode(ClassDeclNode classDeclNode) {
        if(cantAddCodecField(classDeclNode)) return;

        LambdaNode codecCreatorNode = createCodecLambda(classDeclNode);
        classDeclNode.addField(createCodecField(classDeclNode, codecCreatorNode));
        if(classDeclNode.isRecord()) return;

        // Update positions of class members to ensure the flow calculation does not ignore new parameters
        // Records already have their positions updated by the compiler
        PositionUpdater positionUpdater = new PositionUpdater(holder.getContext());
        positionUpdater.updatePositions(classDeclNode.getTree(), classDeclNode.getTree().getStartPosition());
    }

    @Override
    protected void visitCompilationUnitNode(CompilationUnitNode compilationUnitNode) {
        for (String importPath : NEEDED_IMPORTS) {
            createImportIfMissing(compilationUnitNode, importPath);
        }
        for (ClassDeclNode classDeclNode : compilationUnitNode.getClasses()) {
            if (!classDeclNode.annotationPresent(AutoCodec.class)) continue;

            List<IncludedField> includedFields = getIncludedFields(classDeclNode);
            if (includedFields.stream().anyMatch(IncludedField::isOptional)) {
                createImportIfMissing(compilationUnitNode, "java.util.Optional");
            }
        }
    }

    private void createImportIfMissing(CompilationUnitNode compilationUnitNode, String importPath) {
        ImportNode importNode = compilationUnitNode.findImport(importPath);
        if(importNode == null) {
            ImportNode newImport = createImport(importPath);
            compilationUnitNode.addImport(newImport);
        }
    }

    private ImportNode createImport(String importPath) {
        return ImportNode.builder(holder.getContext())
                .importPath(importPath)
                .isStatic(false)
                .build();
    }

    private boolean cantAddCodecField(ClassDeclNode classDeclNode) {
        if(!classDeclNode.annotationPresent(AutoCodec.class)) return true;

        List<VariableDeclNode> fields = classDeclNode.getFields();
        if(fields.isEmpty()) return true;
        if(fields.stream().filter(field -> {
            boolean ignore = field.annotationPresent(Ignore.class);
            boolean isStatic = field.isStatic();
            return !(ignore || isStatic);
        }).count() > MAX_FIELDS) {
            holder.printError("Too many fields for codec generation. Max: " + MAX_FIELDS + ", Found: " + fields.size());
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

    private LambdaNode createCodecLambda(ClassDeclNode classDeclNode) {
        List<IncludedField> includedFields = getIncludedFields(classDeclNode);
        createConstructorIfMissing(classDeclNode, includedFields);

        List<CodecReference> includedCodecs = new ArrayList<>();
        for (IncludedField includedField : includedFields) {
            if(!classDeclNode.isRecord()) {
                createGetterIfMissing(classDeclNode, includedField);
            }

            CodecReferenceCreator creator = new CodecReferenceCreator(holder.getContext(), classDeclNode, includedField);
            includedCodecs.add(creator.createReference());
        }

        FieldAccessNode instanceGroupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("instance")
                .name("group")
                .build();

        MethodInvocationNode.Builder instanceGroupInvocationNodeBuilder = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(instanceGroupReferenceNode);
        for (CodecReference reference : includedCodecs) {
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

        VariableDeclNode instanceParameterNode = VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PARAMETER)
                .name("instance")
                .enclosingType("RecordCodecBuilder")
                .type("Instance")
                .genericParam(classDeclNode)
                .noSym()
                .build();

        MethodInvocationNode applyInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(applyReferenceNode)
                .typeArgument(classDeclNode)
                .argument("instance")
                .argument(selfConstructorReferenceNode)
                .build();

        return LambdaNode.builder(holder.getContext())
                .param(instanceParameterNode)
                .body(applyInvocationNode)
                .build();
    }

    private VariableDeclNode createCodecField(ClassDeclNode classDeclNode, LambdaNode codecCreatorNode) {
        FieldAccessNode recordCodecBuilderCreateReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("RecordCodecBuilder")
                .name("create")
                .build();

        MethodInvocationNode recordCodecBuilderCreateInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(recordCodecBuilderCreateReferenceNode)
                .typeArgument(classDeclNode)
                .argument(codecCreatorNode)
                .build();

        return VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL)
                .name(codecFieldName)
                .owner(classDeclNode)
                .module("com.mojang.serialization")
                .type("Codec")
                .genericParam(classDeclNode)
                .initialValue(recordCodecBuilderCreateInvocationNode)
                .build();
    }

    private void createConstructorIfMissing(ClassDeclNode classDeclNode, List<IncludedField> includedFields) {
        List<Type> parameterTypes = new ArrayList<>();
        for (IncludedField includedField : includedFields) {
            parameterTypes.add(includedField.fieldNode().getType());
        }

        MethodDeclNode constructor = classDeclNode.findConstructor(parameterTypes);
        if(constructor == null) {
            ConstructorCreator ctorCreator = classDeclNode.getConstructorCreator();
            ctorCreator.createCtorForFields(holder.getContext(), includedFields);
        }
    }

    private void createGetterIfMissing(ClassDeclNode classDeclNode, IncludedField includedField) {
        VariableDeclNode fieldNode = includedField.fieldNode();
        MethodDeclNode fieldGetter = classDeclNode.findMethod(fieldNode.getName(), fieldNode.getType());
        if(fieldGetter == null) {
            VariableDeclNode field = includedField.fieldNode();

            ReturnNode returnFieldNode = ReturnNode.builder(holder.getContext())
                    .expression(IdentNode.of(holder.getContext(), field.getName()))
                    .build();

            BlockNode fieldReturnBlock = BlockNode.builder(holder.getContext())
                    .statement(returnFieldNode)
                    .build();

            MethodDeclNode getter = MethodDeclNode.builder(holder.getContext())
                    .modifiers(Flags.PRIVATE)
                    .name(field.getName())
                    .returnType(field.getVarType())
                    .body(fieldReturnBlock)
                    .build();

            classDeclNode.addMethod(getter);
        }
    }

    private List<IncludedField> getIncludedFields(ClassDeclNode classDeclNode) {
        List<VariableDeclNode> fields = classDeclNode.getFields();
        List<IncludedField> included = new ArrayList<>();

        for (VariableDeclNode field : fields) {
            if (field.isStatic()) continue;
            if (field.annotationPresent(Ignore.class)) continue;

            included.add(new IncludedField(field, field.getAnnotation(OptionalEntry.class)));
        }

        return included;
    }
}
