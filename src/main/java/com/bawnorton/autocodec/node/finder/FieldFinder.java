package com.bawnorton.autocodec.node.finder;

import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import java.util.List;

public interface FieldFinder {
    List<VariableDeclNode> getFields();

    default VariableDeclNode findField(String name, Type fieldType) {
        List<VariableDeclNode> fields = getFields();
        for (VariableDeclNode field : fields) {
            if (!field.getName().equals(name)) continue;
            if (!field.getType().equals(fieldType)) continue;
            return field;
        }
        return null;
    }
}
