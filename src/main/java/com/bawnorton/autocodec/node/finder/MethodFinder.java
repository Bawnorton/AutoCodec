package com.bawnorton.autocodec.node.finder;

import com.bawnorton.autocodec.node.MethodDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.util.TypeUtils;
import com.sun.tools.javac.code.Type;
import java.util.List;

public interface MethodFinder {
    List<MethodDeclNode> getMethods();

    default MethodDeclNode findMethod(String name, Type returnType) {
        return findMethod(name, returnType, List.of());
    }

    default MethodDeclNode findMethod(String name, Type returnType, List<Type> parameterTypes) {
        List<MethodDeclNode> methods = getMethods();
        for (MethodDeclNode method : methods) {
            if (!method.getName().equals(name)) continue;
            if (!TypeUtils.equal(method.getReturnType(), returnType)) continue;

            List<VariableDeclNode> methodParameters = method.getParameters();
            if (methodParameters.size() != parameterTypes.size()) continue;

            boolean match = true;
            for (int i = 0; i < parameterTypes.size(); i++) {
                Type parameterType = parameterTypes.get(i);
                Type methodParameterType = methodParameters.get(i).getType();
                if (!parameterType.equals(methodParameterType)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                return method;
            }
        }
        return null;
    }

    default MethodDeclNode findConstructor(List<Type> parameterTypes) {
        return findMethod("<init>", Type.noType, parameterTypes);
    }
}
