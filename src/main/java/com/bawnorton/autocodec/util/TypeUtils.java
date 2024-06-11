package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public final class TypeUtils {
    private static final Table<Class<?>, Type, Type> typeCache = HashBasedTable.create();

    /**
     * {@link Type#equals} only checks the type memory address, not the actual type.
     * This method checks if two types are equal by comparing their symbols and type arguments.
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

    /**
     * Equivalent to {@code instanceof}, but for {@link Type} objects.
     */
    public static boolean isOf(ProcessingContext context, Type type, Class<?> clazz) {
        return findType(context, type, clazz) != null;
    }

    /**
     * Checks if a Type is a Class, not to be confused with {@link TypeUtils#isOf}
     */
    public static boolean is(Type type, Class<?> clazz) {
        if(type == Type.noType) return false;
        if(!(type instanceof Type.ClassType classType)) return false;
        return classType.tsym.getQualifiedName().contentEquals(clazz.getName());
    }

    public static Type findType(ProcessingContext context, Type type, Class<?> clazz) {
        Type cached = typeCache.get(clazz, type);
        if (cached != null) return cached;

        if (type == Type.noType) return null;
        if (!(type instanceof Type.ClassType classType)) return null;
        if (classType.tsym.getQualifiedName().contentEquals(clazz.getName())) return classType;

        List<Type> toCheck = classType.interfaces_field == null ? List.nil() : classType.interfaces_field;
        toCheck = classType.supertype_field == null ? toCheck : toCheck.append(classType.supertype_field);
        if(toCheck.isEmpty()) {
            // need to evaluate the parents of this type and go again
            toCheck = List.from(context.types().directSupertypes(type));
        }
        for(Type toCheckType : toCheck) {
            Type nested = findType(context, toCheckType, clazz);
            if(nested != null) {
                return nested;
            }
        }
        return null;
    }
}
