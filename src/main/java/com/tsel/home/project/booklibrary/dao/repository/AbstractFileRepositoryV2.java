package com.tsel.home.project.booklibrary.dao.repository;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.exception.RepositoryConstraintException;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.repository.utils.FileRepositoryUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFileRepositoryV2<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    private static final Logger log = LogManager.getLogger(AbstractFileRepositoryV2.class);

    protected final String storageFileName;
    protected final Map<K, E> repositoryMap;

    public AbstractFileRepositoryV2(Class<E> entityClass) {
        this.storageFileName = ofNullable(FileRepositoryUtils.resolveStorageFileName(this.getClass()))
            .orElseThrow(() -> new IllegalStateException(format("Невозможно определить название файла хранилища для репозитория '%s'", this.getClass().getSimpleName())));

        this.repositoryMap = initEntities(entityClass);
    }

    private Map<K, E> initEntities(Class<E> entityClass) {
        Map<K, E> entitiesMap = new LinkedHashMap<>();
        List<E> entitiesList = FileRepositoryUtils.readStorageFile(this.storageFileName, entityClass);
        for (E entity : entitiesList) {
            entitiesMap.put(entity.getId(), entity);
        }
        return entitiesMap;
    }

    @Override
    public E getById(K id) {
        return repositoryMap.get(id);
    }

    @Override
    public List<E> getAll() {
        return new ArrayList<>(repositoryMap.values());
    }

    @Override
    public void save(E entity) {
        checkConstrains(entity);

        repositoryMap.put(entity.getId(), entity);
        FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
        log.info("Successfully saved entity '{}'", entity);
    }

    @Override
    public void delete(E entity) {
        this.deleteById(entity.getId());
    }

    @Override
    public void deleteById(K id) {
        E removedEntity = repositoryMap.remove(id);
        if (removedEntity != null) {
            FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
            log.info("Successfully deleted entity '{}'", removedEntity);
        }
    }

    protected boolean isNotLinkedValue(Predicate<E> isLinkedEntityPredicate) {
        return repositoryMap.values()
            .stream()
            .noneMatch(isLinkedEntityPredicate);
    }

    private void checkConstrains(E entityForSave) {
        repositoryMap.values().forEach(existedEntity -> checkEntityConstrains(existedEntity, entityForSave));
    }

    /**
     * Проверка пересечений между двумя сущностями
     */
    protected void checkEntityConstrains(E existedEntity, E entityForSave) throws RepositoryConstraintException {
        // FOR OVERWRITE
    }
}
