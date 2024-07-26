package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;
import com.tsel.home.project.booklibrary.repository.FileStorageName;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepository extends AbstractFileRepository<AudioBookSite> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-audio-book-site-storage.txt";

    private static AudioBookSiteRepository INSTANCE;

    public static AudioBookSiteRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AudioBookSiteRepository(DEFAULT_STORAGE_FILE_NAME);
        }
        return INSTANCE;
    }

    protected AudioBookSiteRepository(String storageFileName) {
        super(storageFileName);
    }
}
