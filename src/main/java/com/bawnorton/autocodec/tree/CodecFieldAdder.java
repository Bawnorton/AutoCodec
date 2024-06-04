package com.bawnorton.autocodec.tree;

import com.bawnorton.autocodec.Ignore;
import com.bawnorton.autocodec.Optional;
import com.bawnorton.autocodec.nodes.ClassDeclNode;
import com.bawnorton.autocodec.nodes.CompilationUnitNode;
import com.bawnorton.autocodec.nodes.ImportNode;
import com.bawnorton.autocodec.nodes.VariableDeclNode;
import com.bawnorton.autocodec.util.ProcessingContext;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.List;

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

        return VariableDeclNode.builder(holder.getContext())
                .modifiers(Flags.PUBLIC | Flags.STATIC)
                .name(codecFieldName)
                .type("Codec")
                .genericParam(classDeclNode.getClassDecl().name)
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
