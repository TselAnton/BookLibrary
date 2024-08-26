package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.util.UUID;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepositoryV2 extends AbstractFileRepositoryV2<UUID, AudioBookSite> {

    public static final AudioBookSiteRepositoryV2 INSTANCE = new AudioBookSiteRepositoryV2();

    private AudioBookSiteRepositoryV2() {
        super(AudioBookSite.class, new UUIDIdentifierGenerator());
    }
}