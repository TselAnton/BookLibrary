package com.tsel.home.project.booklibrary.dao.exception;

public class NotNullConstraintException extends RuntimeException {

    public NotNullConstraintException(String entityName, String fieldName) {
        super("Сущность '" + entityName + "' не может быть сохранена с пустым полем '" + fieldName + "'");
    }
}
