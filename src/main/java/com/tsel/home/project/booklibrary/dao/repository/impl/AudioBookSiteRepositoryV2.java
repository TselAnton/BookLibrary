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

    public AudioBookSiteRepositoryV2(Path rootPath) {
        super(AudioBookSite.class, new UUIDIdentifierGenerator(), rootPath);
    }

    public AudioBookSiteRepositoryV2() {
        super(AudioBookSite.class, new UUIDIdentifierGenerator(), DEFAULT_REPOSITORY_PATH);
    }

    @Override
    protected void compareEntities(AudioBookSite newEntity, AudioBookSite oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("сайт с аудиокнигами с таким же именем уже существует");
        }
    }
}
