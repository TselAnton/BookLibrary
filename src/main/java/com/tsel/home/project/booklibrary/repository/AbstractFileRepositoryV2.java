package com.tsel.home.project.booklibrary.repository;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.data.BaseEntity;
import com.tsel.home.project.booklibrary.utils.StorageFileUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFileRepositoryV2<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    private static final Logger log = LogManager.getLogger(AbstractFileRepositoryV2.class);

    protected final String storageFileName;
    protected final Map<K, E> repositoryMap;

    public AbstractFileRepositoryV2(Class<E> entityClass) {
        this.storageFileName = ofNullable(StorageFileUtils.resolveStorageFileName(this.getClass()))
            .orElseThrow(() -> new IllegalStateException(format("Невозможно определить название файла хранилища для репозитория '%s'", this.getClass().getSimpleName())));

        this.repositoryMap = initEntities(entityClass);
    }

    private Map<K, E> initEntities(Class<E> entityClass) {
        Map<K, E> entitiesMap = new LinkedHashMap<>();
        List<E> entitiesList = StorageFileUtils.readStorageFile(this.storageFileName, entityClass);
        for (E entity : entitiesList) {
            entitiesMap.put(entity.getKey(), entity);
        }
        return entitiesMap;
    }

    @Override
    public E getByName(K key) {
        return repositoryMap.get(key);
    }

    @Override
    public List<E> getAll() {
        return new ArrayList<>(repositoryMap.values());
    }

    @Override
    public void save(E entity) {
        repositoryMap.put(entity.getKey(), entity);
        StorageFileUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
        log.info("Successfully saved entity '{}'", entity);
    }

    @Override
    public void delete(E entity) {
        if (repositoryMap.remove(entity.getKey()) != null) {
            StorageFileUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
            log.info("Successfully deleted entity '{}'", entity);
        }
    }
}
