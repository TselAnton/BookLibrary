package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;

public class AudioBookSiteRepository extends AbstractFileRepository<AudioBookSite> {

    private static final String DEFAULT_STORAGE_FILE_NAME = "my-library-audio-book-site-storage.txt";
    private static final BookRepository BOOK_REPOSITORY = BookRepository.getInstance();

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

    @Override
    public void delete(AudioBookSite entity) {
        BOOK_REPOSITORY.getAll()
            .stream()
            .filter(book -> !book.getAudiobookSites().isEmpty())
            .forEach(book -> {
                book.getAudiobookSites().remove(entity.getKey());
                BOOK_REPOSITORY.save(book);
            });
        super.delete(entity);
    }
}
