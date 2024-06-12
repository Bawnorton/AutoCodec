package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.AutoCodec;
import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.IncludeInChildren;
import com.bawnorton.autocodec.codec.adapter.entry.EntryAdapter;
import com.bawnorton.autocodec.codec.clazz.CodecableClass;
import com.bawnorton.autocodec.codec.clazz.factory.CtorFactory;
import com.bawnorton.autocodec.codec.entry.CodecEntry;
import com.bawnorton.autocodec.codec.entry.CodecEntryField;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.InheritanceHelper;
import com.bawnorton.autocodec.info.FieldInfo;
import com.bawnorton.autocodec.node.*;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public class CodecAdder extends NodeVisitor {
    private static final int MAX_FIELDS = 16;
    private static final List<String> NEEDED_IMPORTS = List.of(
            "com.mojang.serialization.Codec",
            "com.mojang.serialization.codecs.RecordCodecBuilder"
    );

    private final String codecFieldName;

    public CodecAdder(ProcessingContext context, String codecFieldName) {
        super(context);
        this.codecFieldName = codecFieldName;
    }

    @Override
    public void visitClassDeclNode(ClassDeclNode classDeclNode) {
        if(cantAddCodecField(classDeclNode)) return;

        CodecableClass codecableClass = new CodecableClass(holder.getContext(), classDeclNode);
        LambdaNode codecCreatorNode = createCodecBody(codecableClass);
        codecableClass.addField(createCodecField(codecableClass, codecCreatorNode));
    }

    @Override
    protected void visitCompilationUnitNode(CompilationUnitNode compilationUnitNode) {
        boolean autoCodecPresent = false;
        boolean optionalPresent = false;

        for (ClassDeclNode classDeclNode : compilationUnitNode.getClasses()) {
            if (!classDeclNode.annotationPresent(AutoCodec.class)) continue;

            autoCodecPresent = true;
            List<CodecEntryField> codecEntryFields = new CodecableClass(holder.getContext(), classDeclNode).getEntryFields();
            if (codecEntryFields.stream().anyMatch(CodecEntryField::isOptional)) {
                optionalPresent = true;
            }
        }
        if (!autoCodecPresent) return;

        for (String importPath : NEEDED_IMPORTS) {
            createImportIfMissing(compilationUnitNode, importPath);
        }
        if (!optionalPresent) return;

        createImportIfMissing(compilationUnitNode, "java.util.Optional");
    }

    private void createImportIfMissing(CompilationUnitNode compilationUnitNode, String importPath) {
        ImportNode importNode = compilationUnitNode.findImport(importPath);
        if(importNode == null) {
            compilationUnitNode.addImport(
                    ImportNode.builder(holder.getContext())
                            .importPath(importPath)
                            .isStatic(false)
                            .build()
            );
        }
    }

    private boolean cantAddCodecField(ClassDeclNode classDeclNode) {
        if(!classDeclNode.annotationPresent(AutoCodec.class)) return true;

        List<FieldInfo> fields = classDeclNode.getFields().map(FieldInfo::new);
        if(fields.isEmpty()) {
            fields = InheritanceHelper.getAllParentsFields(holder.getContext(), classDeclNode)
                    .stream()
                    .filter(field -> field.getAnnotation(IncludeInChildren.class) != null)
                    .map(FieldInfo::new)
                    .collect(List.collector());
            if(fields.isEmpty()) {
                return true;
            }
        }
        if(fields.stream().filter(field -> {
            boolean ignore = field.annotationPresent(Ignore.class);
            boolean isStatic = field.isStatic();
            return !(ignore || isStatic);
        }).count() > MAX_FIELDS) {
            holder.printError("Too many fields for codec generation. Max: %d, Found: %d".formatted(MAX_FIELDS, fields.size()));
            return true;
        }

        for (FieldInfo field : fields) {
            if (!field.getName().contentEquals(codecFieldName)) continue;

            Type type = field.getType();
            if (type == null) continue;

            if (type.toString().startsWith("com.mojang.serialization.Codec")) {
                return true;
            }
        }
        return false;
    }

    private LambdaNode createCodecBody(CodecableClass codecableClass) {
        List<CodecEntryField> codecEntryFields = codecableClass.getEntryFields();
        createConstructorIfMissing(codecableClass, codecEntryFields);

        List<CodecEntry> includedCodecs = List.nil();
        for (CodecEntryField codecEntryField : codecEntryFields) {
            if(!codecableClass.classDecl().isRecord()) {
                createGetterIfMissing(codecableClass, codecEntryField);
            }

            EntryAdapter entryAdapter = codecEntryField.getAdapter();
            includedCodecs = includedCodecs.append(entryAdapter.createEntry(codecableClass.classDecl(), codecEntryField.fieldInfo()));
        }

        FieldAccessNode instanceGroupReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("instance")
                .name("group")
                .build();

        MethodInvocationNode.Builder instanceGroupInvocationNodeBuilder = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(instanceGroupReferenceNode);
        for (CodecEntry entry : includedCodecs) {
            instanceGroupInvocationNodeBuilder.argument(entry.codecNode());
        }
        MethodInvocationNode instanceGroupInvocationNode = instanceGroupInvocationNodeBuilder.build();

        FieldAccessNode applyReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected(instanceGroupInvocationNode)
                .name("apply")
                .build();

        MemberReferenceNode selfConstructorReferenceNode = MemberReferenceNode.builder(holder.getContext())
                .mode(MemberReferenceTree.ReferenceMode.NEW)
                .name("<init>")
                .expression(codecableClass.name())
                .build();

        VariableDeclNode instanceParameterNode = VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PARAMETER)
                .name("instance")
                .enclosingType("RecordCodecBuilder")
                .type("Instance")
                .genericParam(codecableClass.classDecl())
                .build();

        MethodInvocationNode applyInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(applyReferenceNode)
                .typeArgument(codecableClass.classDecl())
                .argument("instance")
                .argument(selfConstructorReferenceNode)
                .build();

        return LambdaNode.builder(holder.getContext())
                .param(instanceParameterNode)
                .body(applyInvocationNode)
                .build();
    }

    private VariableDeclNode createCodecField(CodecableClass codecableClass, LambdaNode codecCreatorNode) {
        FieldAccessNode recordCodecBuilderCreateReferenceNode = FieldAccessNode.builder(holder.getContext())
                .selected("RecordCodecBuilder")
                .name("create")
                .build();

        MethodInvocationNode recordCodecBuilderCreateInvocationNode = MethodInvocationNode.builder(holder.getContext())
                .methodSelect(recordCodecBuilderCreateReferenceNode)
                .typeArgument(codecableClass.classDecl())
                .argument(codecCreatorNode)
                .build();

        return VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC | Flags.FINAL)
                .name(codecFieldName)
                .type("Codec")
                .genericParam(codecableClass.classDecl())
                .initialValue(recordCodecBuilderCreateInvocationNode)
                .build();
    }

    private void createConstructorIfMissing(CodecableClass codecableClass, List<CodecEntryField> codecEntryFields) {
        List<Type> parameterTypes = codecEntryFields.map(CodecEntryField::getParameterType);
        MethodDeclNode constructor = codecableClass.findConstructor(parameterTypes);
        if(constructor == null) {
            CtorFactory ctorFactory = codecableClass.getCtorFactory(holder.getContext());
            codecableClass.addMethod(ctorFactory.createCtorForFields(codecEntryFields));
        }
    }

    private void createGetterIfMissing(CodecableClass codecableClass, CodecEntryField codecEntryField) {
        FieldInfo fieldInfo = codecEntryField.fieldInfo();
        MethodDeclNode fieldGetter = codecableClass.findMethod(fieldInfo.getNameString(), fieldInfo.getType());
        if(fieldGetter == null) {
            ReturnNode.Builder returnFieldNodeBuilder = ReturnNode.builder(holder.getContext());
            if(fieldInfo.fromParent()) {
                FieldAccessNode superFieldReference = FieldAccessNode.builder(holder.getContext())
                        .selected("super")
                        .name(fieldInfo.getName())
                        .build();
                returnFieldNodeBuilder.expression(superFieldReference);
            } else {
                returnFieldNodeBuilder.expression(IdentNode.of(holder.getContext(), fieldInfo.getName()));
            }

            BlockNode fieldReturnBlock = BlockNode.builder(holder.getContext())
                    .statement(returnFieldNodeBuilder.build())
                    .build();

            MethodDeclNode getter = MethodDeclNode.builder(holder.getContext())
                    .modifiers(Flags.PRIVATE)
                    .name(fieldInfo.getName())
                    .returnType(fieldInfo.getVartype(holder.getContext()))
                    .body(fieldReturnBlock)
                    .build();

            codecableClass.addMethod(getter);
        }
    }
}
