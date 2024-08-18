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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFileRepositoryV2<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    private static final Logger log = LogManager.getLogger(AbstractFileRepositoryV2.class);

    protected final String entityName;
    protected final String storageFileName;
    protected final Map<K, E> repositoryMap;
    protected final Map<Field, Property> notNullFields;

    private final IdentifierGenerator<K> keyGenerator;

    // TODO: нужно принимать Path в котором будет создаваться файл
    // TODO: тесты на все репозитории
    public AbstractFileRepositoryV2(Class<E> entityClass, IdentifierGenerator<K> keyGenerator) {
        this.keyGenerator = keyGenerator;
        this.entityName = resolveEntityName(entityClass);
        this.repositoryMap = initEntities(entityClass);
        this.storageFileName = resolveStorageFileName();
        this.notNullFields = resolveNotNullFields(entityClass);
    }

    private String resolveStorageFileName() {
        return ofNullable(FileRepositoryUtils.resolveStorageFileName(this.getClass()))
            .orElseThrow(() -> new IllegalStateException(
                format("Невозможно определить название файла хранилища для репозитория сущности '%s'", this.entityName))
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
        Map<Field, Property> notNullFields = new HashMap<>();
        for (Field entityProperty : entityClass.getDeclaredFields()) {
            if (entityProperty.isAnnotationPresent(Property.class)) {
                Property propertyAnnotation = entityProperty.getAnnotation(Property.class);
                if (!propertyAnnotation.nullable()) {
                    entityProperty.setAccessible(true);
                    notNullFields.put(entityProperty, propertyAnnotation);
                }
            }
        }
        return notNullFields;
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
        if (entity.getId() == null) {
            entity.setId(keyGenerator.generate());
        }

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

    protected void compareEntities(E newEntity, E oldEntity) throws ConstraintException {
        // FOR OVERWRITE
    }

    private void checkConstrains(E entity) {
        checkNotNullFields(entity);
        this.repositoryMap.values().forEach(existedEntity -> compareEntities(entity, existedEntity));
    }

    private void checkNotNullFields(E entity) {
        for (Field notNullField : this.notNullFields.keySet()) {
            try {
                if (notNullField.get(entity) == null) {
                    throw new NotNullConstraintException(this.entityName, this.notNullFields.get(notNullField).value());
                }
            } catch (IllegalAccessException e) {
                throw new NotNullConstraintException(this.entityName, this.notNullFields.get(notNullField).value());
            }
        }
    }
}
