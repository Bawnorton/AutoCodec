package com.bawnorton.autocodec.node.container;

import com.bawnorton.autocodec.node.MethodDeclNode;
import com.bawnorton.autocodec.node.VariableDeclNode;
import com.bawnorton.autocodec.util.TypeUtils;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.util.List;

public interface MethodContainer {
    List<MethodDeclNode> getMethods();

    void addMethod(MethodDeclNode method);

    default MethodDeclNode findMethod(String name, Type returnType) {
        return findMethod(name, returnType, List.nil());
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
                if (!TypeUtils.equal(methodParameterType, parameterType)) {
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
