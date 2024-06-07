package com.bawnorton.autocodec.util;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public class TypeUtils {
    /**
     * {@link Type#equals} only checks the type memory address, not the actual type
     */
    public static boolean equal(Type typeA, Type typeB) {
        if (typeA == null || typeB == null) return typeA == typeB;
        if (typeA == typeB) return true;

        // Sanity check the symbols and any type arguments
        Symbol.TypeSymbol symbolA = typeA.tsym;
        Symbol.TypeSymbol symbolB = typeB.tsym;

        boolean sameSymbol = symbolA == symbolB;
        if (!sameSymbol) return false;

        List<Type> typeATypeArgs = typeA.getTypeArguments();
        List<Type> typeBTypeArgs = typeB.getTypeArguments();
        if (typeATypeArgs.size() != typeBTypeArgs.size()) return false;

        for (int i = 0; i < typeATypeArgs.size(); i++) {
            if (!equal(typeATypeArgs.get(i), typeBTypeArgs.get(i))) return false;
        }

        return true;
    }
}
