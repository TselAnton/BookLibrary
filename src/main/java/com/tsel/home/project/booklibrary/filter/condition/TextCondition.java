package com.tsel.home.project.booklibrary.filter.condition;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import java.util.Locale;

/**
 * Сравнивает текст без учёта регистра и различий е/ё.
 * Спецсимволы и слова AND/OR считаются обычным текстом, а не командами поиска.
 * @param operation способ сравнения или проверка заполненности
 * @param query буквальный текст условия
 */
public record TextCondition(TextOperation operation, String query) implements FilterCondition {

    /**
     * Нормализует строку для сравнения и поиска внутри справочного списка.
     * @param value непустая ссылка на исходную строку
     * @return строка в нижнем регистре Locale.ROOT с заменой ё на е
     */
    public static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replace('ё', 'е');
    }

    /**
     * Проверяет нормализованное значение без чтения DTO или изменения состояния.
     * @param value каноническое значение или маркер отсутствия
     * @return удовлетворяет ли значение условию
     */
    @Override
    public boolean matches(Object value) {
        if (operation == TextOperation.EMPTY) {
            return value == MissingValue.EMPTY;
        }
        if (operation == TextOperation.NOT_EMPTY) {
            return !(value instanceof MissingValue);
        }
        if (!(value instanceof String text)) {
            return false;
        }
        String preparedText = normalize(text);
        String preparedQuery = normalize(query);
        return switch (operation) {
            case CONTAINS -> preparedText.contains(preparedQuery);
            case EQUALS -> preparedText.equals(preparedQuery);
            case NOT_CONTAINS -> !preparedText.contains(preparedQuery);
            default -> false;
        };
    }

    /**
     * Формирует читаемую подпись условия для тега с учётом метаданных поля.
     * @param field дескриптор, содержащий подписи и единицы измерения
     * @return текст условия без общего названия поля
     */
    @Override
    public String describe(FilterField field) {
        return operation == TextOperation.EMPTY ? field.metadata().emptyLabel()
            : operation == TextOperation.NOT_EMPTY ? field.metadata().notEmptyLabel()
            : operation + ": «" + query + "»";
    }
}
