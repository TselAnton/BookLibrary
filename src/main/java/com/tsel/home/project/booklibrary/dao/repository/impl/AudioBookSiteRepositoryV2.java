package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import java.util.UUID;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepositoryV2 extends AbstractFileRepositoryV2<UUID, AudioBookSite> {

    private static AudioBookSiteRepositoryV2 INSTANCE;

    public static AudioBookSiteRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioBookSiteRepositoryV2();
        }
        return INSTANCE;
    }

    protected AudioBookSiteRepositoryV2() {
        super(AudioBookSite.class, UUIDIdentifierGenerator.INSTANCE);
    }
}
