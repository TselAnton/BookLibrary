package com.tsel.home.project.booklibrary.filter.processor;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import java.util.List;

/**
 * Задаёт нормализацию и правила обработки Java-типа независимо от интерфейса.
 * Процессор объявляет поддерживаемые типы через FilterTypes и может использовать общий редактор.
 */
public interface FilterProcessor {

    /**
     * Приводит исходное значение к каноническому виду без сохранения ссылок на UI-контролы.
     * @param value исходное значение поля
     * @return значение для сравнения или маркер отсутствия
     */
    Object normalize(Object value);

    /**
     * Сообщает, допускается ли вычисление вариантов по снимкам в фоновом потоке.
     * @return true, если реализация не требует исходного DTO и доступа к JavaFX
     */
    default boolean supportsSnapshots() {
        return false;
    }

    /**
     * Собирает варианты без счётчиков; пустые значения добавляет дескриптор согласно метаданным.
     * @param field дескриптор фильтра
     * @param rows исходные строки или поддерживаемые снимки
     * @return канонические значения для списка
     */
    default List<Object> options(FilterField field, List<?> rows) {
        return field.optionsProvider().options(field, rows);
    }

    /**
     * Проверяет поддерживаемые параметры метаданных при создании дескриптора.
     * @param field настраиваемый фильтр
     * @throws IllegalArgumentException при неподдерживаемых или некорректных параметрах
     */
    default void validate(FilterField field) {
        if (!field.settings().isEmpty()) {
            throw new IllegalArgumentException("Неизвестные параметры фильтра «" + field.label() + "»: " + field.settings().keySet());
        }
    }

    /**
     * Приводит условие к применяемому виду; базовая реализация оставляет его без изменений.
     * Процессор может вернуть null для избыточного ограничения.
     * @param field дескриптор фильтра
     * @param condition редактируемое условие или null
     * @return применяемое условие либо null, если ограничение не требуется
     */
    default FilterCondition canonicalize(FilterField field, FilterCondition condition) {
        return condition;
    }
}
