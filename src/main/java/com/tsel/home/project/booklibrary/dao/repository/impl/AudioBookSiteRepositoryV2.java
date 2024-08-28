package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepositoryV2 extends AbstractFileRepositoryV2<UUID, AudioBookSite> {

    private static AudioBookSiteRepositoryV2 instance;

    public static AudioBookSiteRepositoryV2 getInstance(Path... rootPaths) {
        if (instance == null) {
            instance = new AudioBookSiteRepositoryV2(rootPaths);
        }
        return instance;
    }

    public AudioBookSiteRepositoryV2(Path... rootPaths) {
        super(AudioBookSite.class, new UUIDIdentifierGenerator(), rootPaths);
    }

    @Override
    protected void compareEntities(AudioBookSite newEntity, AudioBookSite oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("сайт с аудиокнигами с таким же именем уже существует");
        }
    }
}
