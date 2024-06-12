package com.bawnorton.autocodec.helper;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public final class GenericHelper {
    /**
     * look for {@code ClassImpl<T, V>} in parents and get {@code [T, V]}
     * @param classImpl The type to get the generic out of
     * @return List will always be null or contain at least one element
     */
    public static List<Type> findGenericsOfClassInParents(ProcessingContext context, Type childType, Class<?> classImpl) {
        Type genericContainer = TypeHelper.findType(context, childType, classImpl);
        if(genericContainer == null) return null;

        if(genericContainer instanceof Type.ClassType classImplType) {
            List<Type> generics = classImplType.typarams_field;
            if (generics == null || generics.isEmpty()) return null;

            return generics;
        }
        return null;
    }

    public static Type getFirstGenericOfClassInParentsOrThrow(ProcessingContext context, Type childType, Class<?> classImpl) {
        List<Type> generics = findGenericsOfClassInParents(context, childType, classImpl);
        if(generics == null) throw new IllegalArgumentException("Unable to determine generic type of \"" + classImpl + "\" from " + childType);

        return generics.head;
    }
}
