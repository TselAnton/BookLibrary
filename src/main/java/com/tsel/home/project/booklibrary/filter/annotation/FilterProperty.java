package com.tsel.home.project.booklibrary.filter.annotation;

import com.tsel.home.project.booklibrary.filter.provider.FilterOptionsProvider;
import com.tsel.home.project.booklibrary.filter.provider.FilterValueProvider;
import com.tsel.home.project.booklibrary.filter.provider.impl.FieldValueProvider;
import com.tsel.home.project.booklibrary.filter.provider.impl.ObservedOptionsProvider;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Описывает представление и допустимые значения одного фильтра.
 * Java-тип здесь не дублируется: его обработка определяется аннотацией процессора {@link FilterTypes}.
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FilterProperty {

    /**
     * Задаёт название группы фильтра и префикс применённого тега.
     * @return пользовательская подпись поля
     */
    String label();

    /**
     * Заголовок общей группы; пустая строка не добавляет отдельный заголовок.
     */
    String group() default "";

    /**
     * Порядок поля в панели; равные значения упорядочиваются по имени Java-поля.
     */
    int order() default 0;

    /**
     * Включает поле в текущий интерфейс без изменения хранимых данных.
     */
    boolean visible() default true;

    /**
     * Переопределяет редактор процессора; пустая строка сохраняет его значение по умолчанию.
     */
    String editor() default "";

    /**
     * Подпись логического значения true.
     */
    String trueLabel() default "Есть";

    /**
     * Подпись логического значения false.
     */
    String falseLabel() default "Нет";

    /**
     * Разрешает вариант отсутствия значения и операции проверки заполненности.
     */
    boolean allowEmpty() default true;

    /**
     * Подпись отсутствующего значения; для цены используется «Бесценно».
     */
    String emptyLabel() default "Не указано";

    /**
     * Подпись проверки наличия значения в редакторе и применённом теге.
     */
    String notEmptyLabel() default "Указано";

    /**
     * Подпись неприменимого признака; пустая строка скрывает этот вариант.
     */
    String notApplicableLabel() default "";

    /**
     * Единица измерения в числовом редакторе и тегах.
     */
    String unit() default "";

    /**
     * Дополнительные параметры процессора в формате key=value; проверяются при создании поля.
     */
    String[] settings() default {};

    /**
     * Источник исходного значения DTO до нормализации процессором.
     */
    Class<? extends FilterValueProvider> valueProvider() default FieldValueProvider.class;

    /**
     * Источник канонических вариантов справочного списка.
     */
    Class<? extends FilterOptionsProvider> optionsProvider() default ObservedOptionsProvider.class;
}
