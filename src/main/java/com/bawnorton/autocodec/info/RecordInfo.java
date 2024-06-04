package com.bawnorton.autocodec.info;

import com.bawnorton.autocodec.nodes.AnnotationNode;
import com.bawnorton.autocodec.nodes.ClassDeclNode;
import com.bawnorton.autocodec.nodes.MethodDeclNode;
import com.bawnorton.autocodec.nodes.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecordInfo {
    private final ClassDeclNode classDeclNode;
    private final MethodDeclNode mainConstructor;
    private final Map<VariableDeclNode, List<AnnotationNode>> paramAnnotations;

    public RecordInfo(ClassDeclNode classDeclNode) {
        if (!classDeclNode.isRecord()) {
            throw new IllegalArgumentException("Class is not a record.");
        }

        this.classDeclNode = classDeclNode;
        this.paramAnnotations = new HashMap<>();
        this.mainConstructor = findMainRecordConstructor();
    }

    private MethodDeclNode findMainRecordConstructor() {
        List<MethodDeclNode> constructors = classDeclNode.getConstructors();
        for (MethodDeclNode constructor : constructors) {
            List<VariableDeclNode> fields = classDeclNode.getFields();
            List<VariableDeclNode> parameters = constructor.getParameters();
            if (fields.size() != parameters.size()) continue;

            for (int i = 0; i < fields.size(); i++) {
                VariableDeclNode field = fields.get(i);
                VariableDeclNode parameter = parameters.get(i);
                paramAnnotations.put(parameter, field.getAnnotations());

                Type fieldType = field.getType();
                Type parameterType = parameter.getType();

                if (!fieldType.equals(parameterType)) {
                    throw new AssertionError("Field and parameter types do not match: Found " + fieldType + " at index " + i + " but expected " + parameterType);
                }
            }
            return constructor;
        }
        throw new AssertionError("Record missing main constructor.");
    }

    public ClassDeclNode getNode() {
        return classDeclNode;
    }

    public MethodDeclNode getMainConstructor() {
        return mainConstructor;
    }

    public List<AnnotationNode> getAssociatedFieldAnnotations(VariableDeclNode parameter) {
        return paramAnnotations.get(parameter);
    }
}
