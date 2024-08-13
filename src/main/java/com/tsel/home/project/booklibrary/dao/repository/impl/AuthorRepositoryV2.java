package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.util.UUID;

@FileStorageName("authorStorage.json")
public class AuthorRepositoryV2 extends AbstractFileRepositoryV2<UUID, Author> {

    private static AuthorRepositoryV2 INSTANCE;

    public static AuthorRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthorRepositoryV2();
        }
        return INSTANCE;
    }

    protected AuthorRepositoryV2() {
        super(Author.class, UUIDIdentifierGenerator.INSTANCE);
    }
}
