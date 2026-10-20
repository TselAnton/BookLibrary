package com.tsel.home.project.booklibrary.dao.schema;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Clock;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Описание сохраняемых полей, включая наследуемые. Строится один раз на запуск. */
public final class EntitySchema {

    private final StorageEntity entity;
    private final List<FieldDefinition> fields = new ArrayList<>();

    public EntitySchema(Class<?> type) {
        entity = type.getAnnotation(StorageEntity.class);
        if (entity == null || entity.value().isBlank() || entity.displayName().isBlank() || entity.version() < 1) {
            throw new IllegalArgumentException("Не определена схема: " + type.getName());
        }
        Set<String> names = new HashSet<>();
        for (Class<?> current = type; current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())
                    || field.isSynthetic() || field.isAnnotationPresent(Deprecated.class)) {
                    continue;
                }
                SerializedName serializedName = field.getAnnotation(SerializedName.class);
                String name = serializedName == null ? field.getName() : serializedName.value();
                List<String> aliases = serializedName == null ? List.of() : List.of(serializedName.alternate());
                List<String> allNames = new ArrayList<>(aliases);
                allNames.add(name);
                for (String candidate : allNames) {
                    if (candidate.isBlank() || !names.add(candidate)) {
                        throw new IllegalArgumentException("Повторное или пустое имя поля: " + candidate);
                    }
                }
                StorageField options = field.getAnnotation(StorageField.class);
                DefaultValueProvider provider = null;
                JsonElement defaultValue = null;
                if (options != null) {
                    if (!options.defaultJson().isEmpty()) {
                        defaultValue = JsonParser.parseString(options.defaultJson());
                    }
                    if (options.provider() != NoDefaultValue.class) {
                        if (defaultValue != null) {
                            throw new IllegalArgumentException("У поля два defaults: " + name);
                        }
                        try {
                            provider = options.provider().getDeclaredConstructor().newInstance();
                        } catch (ReflectiveOperationException e) {
                            throw new IllegalArgumentException("Не удалось создать default provider: " + name, e);
                        }
                    }
                }
                Property property = field.getAnnotation(Property.class);
                boolean nullable = !field.getType().isPrimitive() && (property == null || property.nullable());
                fields.add(new FieldDefinition(name, aliases, field.getGenericType(), nullable,
                    options == null ? NullPolicy.KEEP : options.onNull(), defaultValue, provider));
            }
        }
    }

    public String name() {
        return entity.value();
    }

    public int version() {
        return entity.version();
    }

    public JsonObject normalize(JsonObject source, Clock clock) {
        JsonObject result = new JsonObject();
        for (FieldDefinition field : fields) {
            JsonElement value = source.get(field.name());
            for (String alias : field.aliases()) {
                if (source.has(alias)) {
                    if (value != null && !value.equals(source.get(alias))) {
                        throw new IllegalArgumentException("Конфликт имён поля " + field.name() + " и " + alias);
                    }
                    value = source.get(alias);
                }
            }
            if (value == null || value.isJsonNull() && field.onNull() == NullPolicy.USE_DEFAULT) {
                if (field.provider() != null) {
                    value = field.provider().create(clock);
                } else if (field.defaultValue() != null) {
                    value = field.defaultValue().deepCopy();
                }
            }
            if (value == null) {
                value = JsonNull.INSTANCE;
            }
            if (value.isJsonNull() && !field.nullable()) {
                throw new IllegalArgumentException("Обязательное поле " + field.name() + " не заполнено");
            }
            validateType(value, field.type(), field.name());
            result.add(field.name(), value.deepCopy());
        }
        return result;
    }

    private void validateType(JsonElement value, Type type, String path) {
        if (value.isJsonNull()) {
            return;
        }
        try {
            if (type instanceof ParameterizedType parameterized) {
                if (parameterized.getRawType() == List.class) {
                    if (!value.isJsonArray()) {
                        throw new IllegalArgumentException();
                    }
                    int index = 0;
                    for (JsonElement item : value.getAsJsonArray()) {
                        if (item.isJsonNull()) {
                            throw new IllegalArgumentException("Пустой элемент " + path + "[" + index + "]");
                        }
                        validateType(item, parameterized.getActualTypeArguments()[0], path + "[" + index++ + "]");
                    }
                    return;
                }
                if (parameterized.getRawType() == Map.class && parameterized.getActualTypeArguments()[0] == String.class) {
                    for (Map.Entry<String, JsonElement> entry : value.getAsJsonObject().entrySet()) {
                        if (entry.getValue().isJsonNull()) {
                            throw new IllegalArgumentException("Пустое значение " + path + "." + entry.getKey());
                        }
                        validateType(entry.getValue(), parameterized.getActualTypeArguments()[1], path + "." + entry.getKey());
                    }
                    return;
                }
            }
            if (type == String.class || type == UUID.class) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
                    throw new IllegalArgumentException();
                }
                if (type == UUID.class && !UUID.fromString(value.getAsString()).toString().equalsIgnoreCase(value.getAsString())) {
                    throw new IllegalArgumentException();
                }
            } else if (type == Boolean.class || type == boolean.class) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
                    throw new IllegalArgumentException();
                }
            } else if (type == Integer.class || type == int.class || type == Long.class || type == long.class
                || type == Double.class || type == double.class) {
                if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
                    throw new IllegalArgumentException();
                }
                if (type == Integer.class || type == int.class) {
                    value.getAsBigDecimal().intValueExact();
                } else if (type == Long.class || type == long.class) {
                    value.getAsBigDecimal().longValueExact();
                } else if (!Double.isFinite(value.getAsDouble())) {
                    throw new IllegalArgumentException();
                }
            } else {
                throw new IllegalArgumentException("Неподдерживаемый тип схемы: " + type);
            }
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Поле " + path + ": ожидается " + type.getTypeName(), e);
        }
    }

    private record FieldDefinition(
        String name, List<String> aliases, Type type, boolean nullable, NullPolicy onNull,
        JsonElement defaultValue, DefaultValueProvider provider
    ) {}
}
