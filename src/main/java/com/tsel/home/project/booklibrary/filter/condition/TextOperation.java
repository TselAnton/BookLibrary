package com.tsel.home.project.booklibrary.filter.condition;

/**
 * Перечисляет текстовые операции; подписи заполненности могут переопределяться метаданными поля.
 */
public enum TextOperation {

    CONTAINS("Содержит"),
    EQUALS("Совпадает полностью"),
    NOT_CONTAINS("Не содержит"),
    EMPTY("Не указано"),
    NOT_EMPTY("Указано");

    private final String label;

    TextOperation(String label) {
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
