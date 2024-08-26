package com.tsel.home.project.booklibrary.dao.repository;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.exception.NotNullConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.IdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.repository.utils.FileRepositoryUtils;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFileRepositoryV2<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    private static final Logger log = LogManager.getLogger(AbstractFileRepositoryV2.class);

    private final String entityDisplayName;
    private final String storageFileName;
    private final Map<Field, Property> notNullFields;

    private final IdentifierGenerator<K> keyGenerator;

    private Map<K, E> repositoryMap;

    private boolean isTransactionOpen;
    private Map<K, E> repositoryMapSnapshot;

    // TODO: нужно принимать Path в котором будет создаваться файл
    // TODO: тесты на все репозитории
    protected AbstractFileRepositoryV2(Class<E> entityClass, IdentifierGenerator<K> keyGenerator) {
        this.keyGenerator = keyGenerator;
        this.entityDisplayName = resolveEntityName(entityClass);
        this.storageFileName = resolveStorageFileName();

        this.repositoryMap = initEntities(entityClass);
        this.notNullFields = resolveNotNullFields(entityClass);

        this.repositoryMapSnapshot = null;
        this.isTransactionOpen = false;
    }

    private String resolveStorageFileName() {
        return ofNullable(FileRepositoryUtils.resolveStorageFileName(this.getClass()))
            .orElseThrow(() -> new IllegalStateException(
                format("Невозможно определить название файла хранилища для репозитория сущности '%s'", this.entityDisplayName))
            );
    }

    private String resolveEntityName(Class<E> entityClass) {
        return ofNullable(entityClass.getAnnotation(EntityDisplayName.class))
            .map(EntityDisplayName::value)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new IllegalStateException(
                format("Не определена аннотация @EntityDisplayName для сущности %s", entityClass.getName()))
            );
    }

    private Map<Field, Property> resolveNotNullFields(Class<E> entityClass) {
        Map<Field, Property> notNullFieldsMap = new HashMap<>();
        for (Field entityProperty : entityClass.getDeclaredFields()) {
            if (entityProperty.isAnnotationPresent(Property.class)) {
                Property propertyAnnotation = entityProperty.getAnnotation(Property.class);
                if (!propertyAnnotation.nullable()) {
                    entityProperty.setAccessible(true);
                    notNullFieldsMap.put(entityProperty, propertyAnnotation);
                }
            }
        }
        return notNullFieldsMap;
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
    public K save(E entity) {
        if (entity.getId() == null) {
            entity.setId(keyGenerator.generate());
        }

        checkConstrains(entity);
        repositoryMap.put(entity.getId(), entity);

        if (isTransactionOpen) {
            log.info("Successfully saved entity '{}' in transaction", entity);
        } else {
            FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
            log.info("Successfully saved entity '{}'", entity);
        }

        return entity.getId();
    }

    @Override
    public void delete(E entity) {
        this.deleteById(entity.getId());
    }

    @Override
    public void deleteById(K id) {
        E removedEntity = repositoryMap.remove(id);
        if (removedEntity != null) {
            if (isTransactionOpen) {
                log.info("Deleted entity '{}' in transaction", removedEntity);
            } else {
                FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());
                log.info("Successfully deleted entity '{}'", removedEntity);
            }
        }
    }

    @Override
    public void beginTransaction() {
        if (!this.isTransactionOpen) {
            this.isTransactionOpen = true;
            this.repositoryMapSnapshot = new HashMap<>(this.repositoryMap);
            log.info("Starting transaction");
        }
    }

    @Override
    public void commitTransaction() {
        if (this.isTransactionOpen) {
            FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());

            this.isTransactionOpen = false;
            this.repositoryMapSnapshot = null;
            log.info("Transaction commited");
        }
    }

    @Override
    public void abortTransaction() {
        if (this.isTransactionOpen) {
            this.repositoryMap = new HashMap<>(this.repositoryMapSnapshot);
            FileRepositoryUtils.overwriteStorageFile(this.storageFileName, repositoryMap.values());

            this.isTransactionOpen = false;
            this.repositoryMapSnapshot = null;
            log.info("Transaction aborted");
        }
    }

    protected Map<K, E> getRepositoryMap() {
        return this.repositoryMap;
    }

    protected String getEntityDisplayName() {
        return this.entityDisplayName;
    }

    protected void compareEntities(E newEntity, E oldEntity) throws ConstraintException {
        // FOR OVERWRITE
    }

    private void checkConstrains(E entity) {
        checkNotNullFields(entity);
        this.repositoryMap.values().forEach(existedEntity -> compareEntities(entity, existedEntity));
    }

    private void checkNotNullFields(E entity) {
        for (Entry<Field, Property> notNullFieldEntry : this.notNullFields.entrySet()) {
            Field field = notNullFieldEntry.getKey();
            Property fieldAnnotation = notNullFieldEntry.getValue();

            try {
                if (field.get(entity) == null) {
                    throw new NotNullConstraintException(this.entityDisplayName, fieldAnnotation.value());
                }
            } catch (IllegalAccessException e) {
                throw new NotNullConstraintException(this.entityDisplayName, fieldAnnotation.value());
            }
        }
    }
}
