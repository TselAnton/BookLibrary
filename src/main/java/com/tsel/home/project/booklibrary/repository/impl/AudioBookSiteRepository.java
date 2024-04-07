package com.tsel.home.project.booklibrary.repository.impl;

import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.repository.AbstractFileRepository;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

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

    @Override
    protected void updateNewFields() {
        repositoryMap.values().forEach(book -> book.setId(book.getId() == null ? UUID.randomUUID() : book.getId()));
        updateStorageFile();
    }

    @Override
    protected boolean checkConstrains(AudioBookSite existedEntity, AudioBookSite newEntity) {
        return Objects.equals(newEntity.getName(), existedEntity.getName());
    }

    public Optional<AudioBookSite> getByName(String name) {
        return this.repositoryMap.values()
            .stream()
            .filter(audioBookSite -> audioBookSite.getName().equals(name))
            .findFirst();
    }
}
