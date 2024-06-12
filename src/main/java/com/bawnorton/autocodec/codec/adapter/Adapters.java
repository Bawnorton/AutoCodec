package com.bawnorton.autocodec.codec.adapter;

import com.bawnorton.autocodec.context.ProcessingContext;
import com.bawnorton.autocodec.helper.TypeHelper;
import com.sun.tools.javac.code.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Adapters<T> {
    private final Map<Type, T> adapters = new HashMap<>();
    private final Map<Type, T> inferredAdapters = new HashMap<>();
    private T defaultAdapter;

    public void setDefaultAdapterFactory(T declaredAdapter) {
        defaultAdapter = declaredAdapter;
    }

    public void registerAdapterFactory(Type type, T factory) {
        if (adapters.containsKey(type)) {
            throw new IllegalArgumentException("Class \"" + type.tsym.toString() + "\" has already been registered");
        }
        adapters.put(type, factory);
    }

    public T getAdapterFactory(ProcessingContext context, Type type) {
        if (!adapters.containsKey(type)) {
            T foundAdapter = tryFindAdapter(context, type);
            if (foundAdapter != null) {
                return foundAdapter;
            }
            if (defaultAdapter != null) {
                return defaultAdapter;
            }

            throw new IllegalArgumentException("Class \"" + type.tsym.toString() + "\" does not have a registered fieldInfo adpater");
        }
        return adapters.get(type);
    }

    /**
     * Looks for an adapter that is registed to a child type<br>
     * For example if an {@link ArrayList} adapter is not registered but a {@link List} one is then get that adpater
     */
    private T tryFindAdapter(ProcessingContext context, Type type) {
        return inferredAdapters.computeIfAbsent(type, childType -> {
            Set<Type> registeredAdapterTypes = adapters.keySet();
            for (Type registeredAdapterType : registeredAdapterTypes) {
                if(TypeHelper.isOf(context, type, registeredAdapterType)) {
                    return adapters.get(registeredAdapterType);
                }
            }
            return null;
        });
    }
}
