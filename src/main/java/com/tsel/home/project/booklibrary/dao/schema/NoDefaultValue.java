package com.tsel.home.project.booklibrary.dao.schema;

import com.google.gson.JsonElement;
import java.time.Clock;

public final class NoDefaultValue implements DefaultValueProvider {

    @Override
    public JsonElement create(Clock clock) {
        throw new IllegalStateException("Значение по умолчанию не задано");
    }
}
