package com.tsel.home.project.booklibrary.dao.exception;

public class ConstraintException extends RuntimeException {

    public ConstraintException(String entityName, String errorMessage) {
        super("Сущность '" + entityName + "' не может быть сохранена: " + errorMessage);
    }
}
