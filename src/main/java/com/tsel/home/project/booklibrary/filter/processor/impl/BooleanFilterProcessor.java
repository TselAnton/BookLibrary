package com.tsel.home.project.booklibrary.filter.processor.impl;

import com.tsel.home.project.booklibrary.filter.FilterField;
import com.tsel.home.project.booklibrary.filter.annotation.FilterTypes;
import com.tsel.home.project.booklibrary.filter.condition.ChoiceCondition;
import com.tsel.home.project.booklibrary.filter.condition.FilterCondition;
import com.tsel.home.project.booklibrary.filter.model.MissingValue;
import com.tsel.home.project.booklibrary.filter.processor.FilterProcessor;
import java.util.List;
import javafx.scene.control.CheckBox;

/**
 * Обрабатывает Boolean, boolean и JavaFX CheckBox одинаковым списком независимых вариантов.
 * CheckBox читается при создании снимка на UI-потоке; фон получает только Boolean или маркер отсутствия.
 */
@FilterTypes(value = {Boolean.class, boolean.class, CheckBox.class}, editor = "choices")
public class BooleanFilterProcessor implements FilterProcessor {

    /**
     * Сообщает, допускается ли вычисление вариантов по снимкам в фоновом потоке.
     * @return true, если реализация не требует исходного DTO и доступа к JavaFX
     */
    @Override
    public boolean supportsSnapshots() {
        return true;
    }

    /**
     * Проверяет поддерживаемые параметры метаданных при создании дескриптора.
     * @param field настраиваемый фильтр
     * @throws IllegalArgumentException при неподдерживаемых или некорректных параметрах
     */
    @Override
    public void validate(FilterField field) {
        FilterProcessor.super.validate(field);
        if (field.metadata().trueLabel().isBlank()
            || field.metadata().falseLabel().isBlank()
            || field.metadata().trueLabel().equals(field.metadata().falseLabel())
        ) {
            throw new IllegalArgumentException("Нужны разные непустые подписи значений фильтра «" + field.label() + "»");
        }
    }

    /**
     * Захватывает состояние CheckBox на UI-потоке либо принимает готовый Boolean.
     * @param value флаг, CheckBox или null
     * @return Boolean или маркер отсутствия
     */
    @Override
    public Object normalize(Object value) {
        if (value == null) {
            return MissingValue.EMPTY;
        }
        return value instanceof CheckBox checkBox ? checkBox.isSelected() : value;
    }

    /**
     * Собирает варианты без счётчиков; пустые значения добавляет дескриптор согласно метаданным.
     * @param field дескриптор фильтра
     * @param rows исходные строки или поддерживаемые снимки
     * @return канонические значения для списка
     */
    @Override
    public List<Object> options(FilterField field, List<?> rows) {
        return List.of(true, false);
    }

    /**
     * Удаляет избыточное ограничение, если выбранные варианты охватывают все значения поля.
     * @param field дескриптор фильтра
     * @param condition редактируемое условие или null
     * @return применяемое условие либо null, если ограничение не требуется
     */
    @Override
    public FilterCondition canonicalize(FilterField field, FilterCondition condition) {
        if (condition instanceof ChoiceCondition choice && choice.values().containsAll(field.optionValues(List.of()))) {
            return null;
        }
        return condition;
    }
}
