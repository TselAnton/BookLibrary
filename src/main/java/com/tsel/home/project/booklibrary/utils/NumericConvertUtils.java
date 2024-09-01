package com.tsel.home.project.booklibrary.utils;

import static com.tsel.home.project.booklibrary.utils.StringUtils.isNotBlank;

public final class NumericConvertUtils {

    private NumericConvertUtils() {}

    public static Integer stringToInteger(String value) {
        try {
            return isNotBlank(value)
                ? Integer.parseInt(value.trim())
                : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Double stringToDouble(String value) {
        try {
            return isNotBlank(value)
                ? Double.parseDouble(value.trim().replace(",", "."))
                : null;

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
