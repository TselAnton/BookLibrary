package com.tsel.home.project.booklibrary.dao.schema;

import com.google.gson.JsonElement;
import java.time.Clock;

public interface DefaultValueProvider {

    JsonElement create(Clock clock);
}
