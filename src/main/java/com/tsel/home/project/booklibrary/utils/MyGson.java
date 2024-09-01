package com.tsel.home.project.booklibrary.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class MyGson {

    private MyGson() {}

    public static Gson buildGson() {
        return new GsonBuilder()
            .addSerializationExclusionStrategy(new GsonExclusionStrategy())
            .serializeNulls()
            .create();
    }

    private static class GsonExclusionStrategy implements ExclusionStrategy {

        @Override
        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Deprecated.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> aClass) {
            return false;
        }
    }
}
