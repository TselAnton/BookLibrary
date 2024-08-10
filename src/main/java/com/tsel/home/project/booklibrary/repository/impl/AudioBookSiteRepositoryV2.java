package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepositoryV2;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepositoryV2 extends AbstractFileRepositoryV2<String, AudioBookSite> {

    private static AudioBookSiteRepositoryV2 INSTANCE;

    public static AudioBookSiteRepositoryV2 getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioBookSiteRepositoryV2();
        }
        return INSTANCE;
    }

    protected AudioBookSiteRepositoryV2() {
        super(AudioBookSite.class);
    }
}
