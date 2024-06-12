package com.bawnorton.autocodec.util;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;
import java.util.HashSet;
import java.util.Set;

public final class TypeUtils {
    private static final Table<Type, Type, Type> typeCache = HashBasedTable.create();

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

    public static Type typeOf(ProcessingContext context, Class<?> clazz) {
        return (Type) context.elements().getTypeElement(clazz.getCanonicalName()).asType();
    }

    /**
     * Equivalent to {@code instanceof}, but for {@link Type} objects.
     */
    public static boolean isOf(ProcessingContext context, Type parentType, Class<?> childClazz) {
        return findType(context, parentType, childClazz) != null;
    }

    /**
     * Equivalent to {@code instanceof}, but for {@link Type} objects.
     */
    public static boolean isOf(ProcessingContext context, Type parentType, Type childType) {
        return findType(context, parentType, childType) != null;
    }

    /**
     * Checks if a {@link Type} is a {@link Class}, not to be confused with {@link TypeUtils#isOf}
     */
    public static boolean is(Type type, Class<?> clazz) {
        if(type == Type.noType) return false;
        if(!(type instanceof Type.ClassType classType)) return false;
        return classType.tsym.getQualifiedName().contentEquals(clazz.getName());
    }

    /**
     * Find a {@link Type} for {@code childClass} that exists in the heirachy of {@code type}
     *
     * @param parentType The type at the top of the hierachy to search down in
     * @param childClass The class to look for in the hierachy
     */
    public static Type findType(ProcessingContext context, Type parentType, Class<?> childClass) {
        Type childType = (Type) context.elements().getTypeElement(childClass.getCanonicalName()).asType();
        return findType(context, parentType, childType);
    }

    public static Type findType(ProcessingContext context, Type parentType, Type childType) {
        Type cached = typeCache.get(parentType, childType);
        if (cached != null) return cached;
        if (!(parentType instanceof Type.ClassType classType)) {
            return null;
        }

        Set<Type> visited = new HashSet<>();
        return findTypeRecursively(context, classType, childType, visited);
    }

    private static Type findTypeRecursively(ProcessingContext context, Type parentType, Type childType, Set<Type> visited) {
        if (parentType == Type.noType || !visited.add(parentType)) {
            return null;
        }

        if (!(parentType instanceof Type.ClassType classType)) {
            return null;
        }
        if (classType.tsym.getQualifiedName().equals(childType.tsym.getQualifiedName())) {
            return classType;
        }

        List<Type> supertypes = List.from(context.types().directSupertypes(parentType));
        for (Type supertype : supertypes) {
            Type result = findTypeRecursively(context, supertype, childType, visited);
            if (result != null) {
                typeCache.put(childType, parentType, result);
                return result;
            }
        }
        return null;
    }

    /**
     * Determine if a {@link Type} is a child of {@code parentClass}. Not to be confused with {@link TypeUtils#isOf}
     *
     * @param childType   The childType at the bottom of the heirachy to search up from
     * @param parentClass The class to look for in the heirachy
     */
    public static boolean isChildOf(ProcessingContext context, Type childType, Class<?> parentClass) {
        Type parentType = (Type) context.elements().getTypeElement(parentClass.getCanonicalName()).asType();
        return findType(context, parentType, childType) != null;
    }
}
