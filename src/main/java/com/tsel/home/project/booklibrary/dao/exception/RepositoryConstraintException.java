package com.tsel.home.project.booklibrary.dao.exception;

import static java.lang.String.format;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;

public class RepositoryConstraintException extends RuntimeException {

    // TODO: переделать
    public RepositoryConstraintException(BaseEntity<?> baseEntity, String reason) {
        super(format("%s не может быть сохранена, потому что: %s", baseEntity.getEntityPrintName(), reason));
    }
}
