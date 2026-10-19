package com.tsel.home.project.booklibrary.filter.condition;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import java.math.BigDecimal;

/**
 * Сравнивает числа точно через BigDecimal; диапазон включает обе границы.
 * Отсутствующее значение отличается от нуля и не удовлетворяет числовым сравнениям.
 * @param operation сравнение, диапазон или проверка заполненности
 * @param first значение сравнения или нижняя граница; null допускается для открытого диапазона
 * @param second верхняя граница диапазона; null означает отсутствие верхнего ограничения
 */
public record NumericCondition(NumericOperation operation, BigDecimal first, BigDecimal second) implements FilterCondition {

    /**
     * Проверяет нормализованное значение без чтения DTO или изменения состояния.
     * @param value каноническое значение или маркер отсутствия
     * @return удовлетворяет ли значение условию
     */
    @Override
    public boolean matches(Object value) {
        if (operation == NumericOperation.EMPTY) {
            return value == MissingValue.EMPTY;
        }
        if (operation == NumericOperation.NOT_EMPTY) {
            return !(value instanceof MissingValue);
        }
        if (!(value instanceof BigDecimal number)) {
            return false;
        }
        return switch (operation) {
            case RANGE -> (first == null || number.compareTo(first) >= 0)
                && (second == null || number.compareTo(second) <= 0);
            case EQUALS -> number.compareTo(first) == 0;
            case NOT_EQUALS -> number.compareTo(first) != 0;
            case GREATER -> number.compareTo(first) > 0;
            case AT_LEAST -> number.compareTo(first) >= 0;
            case LESS -> number.compareTo(first) < 0;
            case AT_MOST -> number.compareTo(first) <= 0;
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
        String text = switch (operation) {
            case RANGE -> first == null ? "до " + format(second)
                : second == null ? "от " + format(first)
                : format(first) + "–" + format(second);
            case EMPTY -> field.metadata().emptyLabel();
            case NOT_EMPTY -> field.metadata().notEmptyLabel();
            default -> operation + " " + format(first);
        };
        return field.metadata().unit().isBlank() || operation == NumericOperation.EMPTY || operation == NumericOperation.NOT_EMPTY
            ? text : text + " " + field.metadata().unit();
    }

    private String format(BigDecimal number) {
        return number.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
