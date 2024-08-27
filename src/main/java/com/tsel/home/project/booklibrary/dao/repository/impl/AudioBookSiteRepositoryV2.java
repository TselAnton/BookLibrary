package com.tsel.home.project.booklibrary.dao.repository.impl;

import com.tsel.home.project.booklibrary.dao.annotation.FileStorageName;
import com.tsel.home.project.booklibrary.dao.data.AudioBookSite;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.IdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.identifier.UUIDIdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.AbstractFileRepositoryV2;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

@FileStorageName("audioBookSiteStorage.json")
public class AudioBookSiteRepositoryV2 extends AbstractFileRepositoryV2<UUID, AudioBookSite> {

    public static final AudioBookSiteRepositoryV2 INSTANCE = new AudioBookSiteRepositoryV2(AudioBookSite.class, new UUIDIdentifierGenerator(), null);

    public AudioBookSiteRepositoryV2(Class<AudioBookSite> entityClass, IdentifierGenerator<UUID> keyGenerator, @Nullable Path rootPath) {
        super(entityClass, keyGenerator, rootPath);
    }

    @Override
    protected void compareEntities(AudioBookSite newEntity, AudioBookSite oldEntity) throws ConstraintException {
        if (Objects.equals(oldEntity.getName(), newEntity.getName())) {
            throw buildConstraintException("сайт с аудиокнигами с таким же именем уже существует");
        }
    }
}
