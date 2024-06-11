package com.bawnorton.autocodec.node.container;

import com.bawnorton.autocodec.node.VariableDeclNode;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public interface FieldContainer {
    List<VariableDeclNode> getFields();

    void addField(VariableDeclNode field);

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
