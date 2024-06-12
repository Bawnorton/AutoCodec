package com.bawnorton.autocodec.helper;

import com.bawnorton.autocodec.IncludeInChildren;
import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.node.ClassDeclNode;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class InheritanceHelper {
    public static List<Symbol.VarSymbol> getParentFields(ProcessingContext context, ClassDeclNode child) {
        List<Symbol.VarSymbol> parentFields = List.nil();
        Symbol.ClassSymbol superClassSymbol = getParentSymbol(context, child);
        if (superClassSymbol == null) return parentFields;

        Iterable<Symbol> symbols = superClassSymbol.members().getSymbols();
        for (Symbol symbol : symbols) {
            if (!(symbol instanceof Symbol.VarSymbol varSymbol)) continue;
            parentFields = parentFields.append(varSymbol);
        }
        return parentFields;
    }

    public static List<Symbol.VarSymbol> getAllParentsFields(ProcessingContext context, ClassDeclNode child) {
        Symbol.ClassSymbol superClassSymbol = getParentSymbol(context, child);
        List<Symbol.VarSymbol> parentFields = List.nil();
        while (superClassSymbol != null) {
            for(Symbol symbol : superClassSymbol.members().getSymbols()) {
                if (!(symbol instanceof Symbol.VarSymbol varSymbol)) continue;
                parentFields = parentFields.append(varSymbol);
            }

            if (!(superClassSymbol.getSuperclass() instanceof Type.ClassType nextParentClassType)) break;
            if (!(nextParentClassType.tsym instanceof Symbol.ClassSymbol nextParentSymbol)) break;

            superClassSymbol = nextParentSymbol;
        }
        return parentFields;
    }

    public static List<Symbol.MethodSymbol> getParentConstructors(ProcessingContext context, ClassDeclNode child) {
        List<Symbol.MethodSymbol> parentCtors = List.nil();
        Symbol.ClassSymbol superClassSymbol = getParentSymbol(context, child);
        if (superClassSymbol == null) return parentCtors;

        Iterable<Symbol> symbols = superClassSymbol.members().getSymbols();
        for (Symbol symbol : symbols) {
            if (!(symbol instanceof Symbol.MethodSymbol methodSymbol)) continue;
            if (!methodSymbol.isConstructor()) continue;

            parentCtors = parentCtors.append(methodSymbol);
        }
        return parentCtors;
    }

    private static Symbol.ClassSymbol getParentSymbol(ProcessingContext context, ClassDeclNode child) {
        Type.ClassType superClassType = child.getSuperClassType();
        if (superClassType == null) return null;
        if (superClassType.tsym.getQualifiedName().equals(context.names().java_lang_Object)) return null;
        if (!(superClassType.tsym instanceof Symbol.ClassSymbol classSymbol)) return null;

        return classSymbol;
    }
}
