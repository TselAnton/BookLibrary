package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Author;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("authorStorage.json")
public class AuthorRepositoryV2 extends AbstractFileRepositoryV2<String, Author> {

    private static AuthorRepositoryV2 INSTANCE;

    public static AuthorRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AuthorRepositoryV2();
        }
        return INSTANCE;
    }

    protected AuthorRepositoryV2() {
        super(Author.class);
    }
}
