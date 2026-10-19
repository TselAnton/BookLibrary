package com.tsel.home.project.booklibrary.filter.condition;

/**
 * Перечисляет числовые сравнения и проверки заполненности; поиск подстроки цифр не поддерживается.
 */
public enum NumericOperation {

    RANGE("Диапазон"),
    EQUALS("Равно"),
    NOT_EQUALS("Не равно"),
    GREATER("Больше"),
    AT_LEAST("Не меньше"),
    LESS("Меньше"),
    AT_MOST("Не больше"),
    EMPTY("Не указано"),
    NOT_EMPTY("Указано");

    private final String label;

    NumericOperation(String label) {
        this.label = label;
    }

    /**
     * Возвращает стандартную подпись операции; метаданные поля могут уточнять подписи заполненности.
     */
    @Override
    public String toString() {
        return label;
    }
}
