package com.tsel.home.project.booklibrary.dao.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.time.Clock;

public final class CurrentTimeDefault implements DefaultValueProvider {

    @Override
    public JsonElement create(Clock clock) {
        return new JsonPrimitive(clock.millis());
    }
}
