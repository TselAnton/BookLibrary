package com.tsel.home.project.booklibrary.utils;

public final class StringUtils {

    private StringUtils() {}

    public static boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean isBlank(String value) {
        return !isNotBlank(value);
    }
}
