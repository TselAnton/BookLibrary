package com.tsel.home.project.booklibrary.filter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Объявляет Java-типы, поддерживаемые процессором, и его редактор по умолчанию.
 * Новый тип может повторно использовать существующий процессор и редактор.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterTypes {

    /**
     * Задаёт точные Java-типы, за которые отвечает процессор.
     * @return типы без повторений в реестре
     */
    Class<?>[] value();

    /**
     * Задаёт редактор по умолчанию для всех объявленных типов.
     * @return идентификатор фабрики UI-редактора
     */
    String editor();
}
