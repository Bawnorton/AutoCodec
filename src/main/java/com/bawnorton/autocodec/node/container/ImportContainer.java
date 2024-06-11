package com.bawnorton.autocodec.node.container;

import com.bawnorton.autocodec.node.ImportNode;
import com.sun.tools.javac.util.List;

public interface ImportContainer {
    List<ImportNode> getImports();

    void addImport(ImportNode importNode);

    default ImportNode findImport(String name) {
        List<ImportNode> imports = getImports();
        for (ImportNode importNode : imports) {
            String qualifier = importNode.getImportPath();
            String[] parts = qualifier.split("\\.");
            String[] nameParts = name.split("\\.");
            boolean match = true;
            for (int i = 0; i < parts.length; i++) {
                if (i >= nameParts.length) {
                    match = false;
                    break;
                }
                if (!parts[i].equals(nameParts[i]) && !parts[i].equals("*")) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return importNode;
            }
        }
        return null;
    }
}
