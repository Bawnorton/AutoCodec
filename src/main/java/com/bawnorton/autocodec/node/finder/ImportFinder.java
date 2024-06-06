package com.bawnorton.autocodec.node.finder;

import com.bawnorton.autocodec.node.ImportNode;
import java.util.List;

public interface ImportFinder {
    List<ImportNode> getImports();

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
