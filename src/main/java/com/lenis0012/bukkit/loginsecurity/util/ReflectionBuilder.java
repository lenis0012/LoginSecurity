package com.lenis0012.bukkit.loginsecurity.util;

import lombok.SneakyThrows;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class ReflectionBuilder {
    private final Class<?> clazz;
    private final Object instance;

    @SneakyThrows
    public ReflectionBuilder(String className) {
        this.clazz = Class.forName(className);
        this.instance = clazz.newInstance();
    }

    @SneakyThrows
    public ReflectionBuilder call(String methodName, Object... args) {
        Optional<Method> matchingMethod = Arrays.stream(clazz.getMethods())
            .filter(method -> method.getName().equals(methodName))
            .filter(method -> method.getParameterCount() == args.length)
            .filter(method -> methodSignatureMatches(method, args))
            .findFirst();

        matchingMethod.get().invoke(instance, args);
        return this;
    }

    public Object build() {
        return instance;
    }

    public <T> T build(Class<T> type) {
        return type.cast(instance);
    }

    private static boolean methodSignatureMatches(Method method, Object[] args) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        for(int i = 0; i < parameterTypes.length; i++) {
            if(!parameterTypes[i].isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    public static boolean classExists(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
