package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.Publisher;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("publisherStorage.json")
public class PublisherRepositoryV2 extends AbstractFileRepositoryV2<String, Publisher> {

    private static PublisherRepositoryV2 INSTANCE;

    public static PublisherRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PublisherRepositoryV2();
        }
        return INSTANCE;
    }

    protected PublisherRepositoryV2() {
        super(Publisher.class);
    }
}
