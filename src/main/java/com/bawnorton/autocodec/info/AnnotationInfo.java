package com.bawnorton.autocodec.info;

import com.bawnorton.autocodec.node.AnnotationNode;
import com.bawnorton.autocodec.node.LiteralNode;
import com.bawnorton.autocodec.util.Or;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public final class AnnotationInfo {
    private final Or<AnnotationNode, AnnotationWrapper> annotation;

    public AnnotationInfo(AnnotationNode annotation) {
        this.annotation = Or.left(annotation);
    }

    public AnnotationInfo(Annotation annotation) {
        this.annotation = Or.right(new AnnotationWrapper(annotation));
    }

    public boolean hasValue() {
        if (annotation.isLeft()) {
            return annotation.left().hasValue();
        } else {
            return annotation.right().hasValue();
        }
    }

    public <T> T getLiteralValue(Class<T> type) {
        if (annotation.isLeft()) {
            return annotation.left().getValue(LiteralNode.class).getValue(type);
        } else {
            return annotation.right().getValue(type);
        }
    }

    private static class AnnotationWrapper {
        private final Map<String, Object> args = new HashMap<>();

        private AnnotationWrapper(Annotation annotation) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            Method[] annotationMethods = annotationType.getDeclaredMethods();
            for (Method method : annotationMethods) {
                String name = method.getName();
                Object result;
                try {
                    result = method.invoke(annotation);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    continue;
                }
                args.put(name, result);
            }
        }

        public boolean hasArg(String name) {
            return args.containsKey(name);
        }

        public <T> T getArg(String name, Class<T> type) {
            return type.cast(args.get(name));
        }

        public boolean hasValue() {
            return hasArg("value");
        }

        public <T> T getValue(Class<T> type) {
            return getArg("value", type);
        }
    }
}
