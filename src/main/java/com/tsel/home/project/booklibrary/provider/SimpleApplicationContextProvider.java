package com.tsel.home.project.booklibrary.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class SimpleApplicationContextProvider {

    private static final Map<Class<?>, ? super Object> APPLICATION_CONTEXT = new HashMap<>();

    private SimpleApplicationContextProvider() {}

    public static <T> T getBean(Class<T> beanClass) {
        return beanClass.cast(APPLICATION_CONTEXT.get(beanClass));
    }

    public static <T> void initBean(Class<T> beanClass, Supplier<T> initSupplier) {
        APPLICATION_CONTEXT.putIfAbsent(beanClass, initSupplier.get());
    }

    public static <T> void replaceBean(Class<T> beanClass, Supplier<T> initSupplier) {
        APPLICATION_CONTEXT.put(beanClass, initSupplier.get());
    }
}
