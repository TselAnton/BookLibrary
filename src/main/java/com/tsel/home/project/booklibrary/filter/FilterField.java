package com.tsel.home.project.booklibrary.filter;

import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.model.FilterRow;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessor;
import com.tsel.home.project.booklibrary.filter.provider.FilterOptionsProvider;
import com.tsel.home.project.booklibrary.filter.provider.FilterValueProvider;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Связывает Java-поле, метаданные, процессор и источники значений.
 * Чтение исходного DTO выполняется в UI-потоке; готовый снимок читается без обращения к контролам.
 */
public class FilterField {

    private final Field field;
    private final FilterProperty metadata;
    private final FilterProcessor processor;
    private final FilterValueProvider valueProvider;
    private final FilterOptionsProvider optionsProvider;
    private final Map<String, String> settings;

    /**
     * Создаёт источники значений и проверяет метаданные до построения интерфейса.
     * @param field аннотированное Java-поле
     * @param processor процессор его Java-типа
     */
    public FilterField(Field field, FilterProcessor processor) {
        this.field = field;
        this.metadata = field.getAnnotation(FilterProperty.class);
        this.processor = processor;
        field.setAccessible(true);
        try {
            valueProvider = metadata.valueProvider().getDeclaredConstructor().newInstance();
            optionsProvider = metadata.optionsProvider().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Не удалось создать источник фильтра «" + label() + "»", e);
        }
        Map<String, String> parsedSettings = new LinkedHashMap<>();
        for (String setting : metadata.settings()) {
            int separator = setting.indexOf('=');
            if (separator < 1 || parsedSettings.putIfAbsent(setting.substring(0, separator), setting.substring(separator + 1)) != null) {
                throw new IllegalArgumentException("Некорректный параметр фильтра «" + label() + "»: " + setting);
            }
        }
        settings = Map.copyOf(parsedSettings);
        if (metadata.allowEmpty() && (metadata.emptyLabel().isBlank() || metadata.notEmptyLabel().isBlank())) {
            throw new IllegalArgumentException("Не задана подпись пустого значения фильтра «" + label() + "»");
        }
        processor.validate(this);
    }

    /**
     * Возвращает стабильный идентификатор фильтра: имя Java-поля.
     * @return ключ условия в состоянии и фоновом снимке
     */
    public String id() {
        return field.getName();
    }

    /**
     * Возвращает подпись поля для пользователя.
     * @return название группы и префикс тега
     */
    public String label() {
        return metadata.label();
    }

    /**
     * Возвращает объявленные параметры единственной аннотации поля.
     * @return метаданные представления и источников
     */
    public FilterProperty metadata() {
        return metadata;
    }

    /**
     * Возвращает процессор, выбранный по Java-типу.
     * @return обработчик нормализации и правил ввода
     */
    public FilterProcessor processor() {
        return processor;
    }

    /**
     * Возвращает настроенный источник справочных значений.
     * @return провайдер вариантов без счётчиков
     */
    public FilterOptionsProvider optionsProvider() {
        return optionsProvider;
    }

    /**
     * Возвращает проверенные параметры расширения в формате ключ — значение.
     * @return неизменяемая карта настроек
     */
    public Map<String, String> settings() {
        return settings;
    }

    /**
     * Выбирает редактор из метаданных поля или аннотации процессора.
     * @return идентификатор фабрики в реестре редакторов
     */
    public String editor() {
        return metadata.editor().isBlank()
            ? processor.getClass().getAnnotation(FilterTypes.class).editor()
            : metadata.editor();
    }

    /**
     * Читает и нормализует DTO либо получает готовое значение из снимка.
     * @param row исходный DTO или FilterRow
     * @return каноническое значение для условия
     */
    public Object value(Object row) {
        if (row instanceof FilterRow snapshot) {
            return snapshot.values().get(id());
        }
        try {
            return processor.normalize(valueProvider.read(row, field));
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Не удалось прочитать фильтр «" + label() + "»", e);
        }
    }

    /**
     * Дополняет варианты процессора разрешёнными маркерами пустоты и неприменимости.
     * @param rows каталог для получения справочных значений
     * @return уникальные канонические варианты
     */
    public List<Object> optionValues(List<?> rows) {
        List<Object> values = new ArrayList<>(processor.options(this, rows));
        if (metadata.allowEmpty()) {
            values.add(MissingValue.EMPTY);
        }
        if (!metadata.notApplicableLabel().isBlank()) {
            values.add(MissingValue.NOT_APPLICABLE);
        }
        return values.stream().distinct().toList();
    }

    /**
     * Преобразует значение в подпись, включая false, пустоту и неприменимость.
     * @param value каноническое значение
     * @return локализованная подпись варианта
     */
    public String optionLabel(Object value) {
        if (value == MissingValue.EMPTY) {
            return metadata.emptyLabel();
        }
        if (value == MissingValue.NOT_APPLICABLE) {
            return metadata.notApplicableLabel();
        }
        if (value instanceof Boolean flag) {
            return flag ? metadata.trueLabel() : metadata.falseLabel();
        }
        return String.valueOf(value);
    }
}
