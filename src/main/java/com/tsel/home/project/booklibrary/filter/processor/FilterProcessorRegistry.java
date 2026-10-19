package com.tsel.home.project.booklibrary.filter.processor;

import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.processor.impl.BooleanFilterProcessor;
import com.tsel.home.project.booklibrary.filter.processor.impl.IntegerFilterProcessor;
import com.tsel.home.project.booklibrary.filter.processor.impl.NumericFilterProcessor;
import com.tsel.home.project.booklibrary.filter.processor.impl.StringFilterProcessor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сопоставляет точные Java-типы с процессорами из их аннотаций.
 * Регистрация происходит при создании каталога; неоднозначные назначения запрещены.
 */
public class FilterProcessorRegistry {

    private final Map<Class<?>, FilterProcessor> processors = new LinkedHashMap<>();

    /**
     * Создаёт независимый реестр со стандартными реализациями приложения.
     * @return реестр, который можно дополнить перед созданием каталога
     */
    public static FilterProcessorRegistry defaults() {
        FilterProcessorRegistry registry = new FilterProcessorRegistry();
        registry.register(new BooleanFilterProcessor());
        registry.register(new StringFilterProcessor());
        registry.register(new NumericFilterProcessor());
        registry.register(new IntegerFilterProcessor());
        return registry;
    }

    /**
     * Регистрирует обработчик, запрещая повторные или некорректные назначения.
     * @throws IllegalArgumentException при конфликте регистрации
     */
    public void register(FilterProcessor processor) {
        FilterTypes types = processor.getClass().getAnnotation(FilterTypes.class);
        if (types == null || types.value().length == 0 || types.editor().isBlank()) {
            throw new IllegalArgumentException("Процессор должен объявлять типы и редактор через @FilterTypes: " + processor.getClass().getName());
        }
        for (Class<?> type : types.value()) {
            if (processors.containsKey(type)) {
                throw new IllegalArgumentException("Несколько процессоров фильтра для типа " + type.getName());
            }
        }
        for (Class<?> type : types.value()) {
            processors.put(type, processor);
        }
    }

    /**
     * Находит процессор по точному Java-типу; неоднозначные преобразования не выполняются.
     * @param type Java-тип поля
     * @return зарегистрированный процессор
     * @throws IllegalArgumentException если тип не зарегистрирован
     */
    public FilterProcessor resolve(Class<?> type) {
        FilterProcessor processor = processors.get(type);
        if (processor == null) {
            throw new IllegalArgumentException("Не зарегистрирован процессор фильтра для типа " + type.getName());
        }
        return processor;
    }
}
