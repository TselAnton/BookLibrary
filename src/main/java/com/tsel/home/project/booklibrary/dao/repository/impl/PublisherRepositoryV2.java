package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.util.UUID;

@FileStorageName("publisherStorage.json")
public class PublisherRepositoryV2 extends AbstractFileRepositoryV2<UUID, Publisher> {

    private static PublisherRepositoryV2 INSTANCE;

    public static PublisherRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PublisherRepositoryV2();
        }
        return INSTANCE;
    }

    protected PublisherRepositoryV2() {
        super(Publisher.class, UUIDIdentifierGenerator.INSTANCE);
    }
}
