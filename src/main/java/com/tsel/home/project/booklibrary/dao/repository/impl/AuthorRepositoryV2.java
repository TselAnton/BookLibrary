package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.util.UUID;

@FileStorageName("authorStorage.json")
public class AuthorRepositoryV2 extends AbstractFileRepositoryV2<UUID, Author> {

    public static final AuthorRepositoryV2 INSTANCE = new AuthorRepositoryV2();

    protected AuthorRepositoryV2() {
        super(Author.class, new UUIDIdentifierGenerator());
    }
}
