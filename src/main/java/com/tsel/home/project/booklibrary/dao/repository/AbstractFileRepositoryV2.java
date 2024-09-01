package com.tsel.home.project.booklibrary.dao.repository;

import static com.tsel.home.project.booklibrary.utils.FileUtils.buildPathFromCurrentDir;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.exception.NotNullConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.IdentifierGenerator;
import com.tsel.home.project.booklibrary.provider.FileRepositoryProvider;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFileRepositoryV2<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    public static final Path DEFAULT_REPOSITORY_PATH = buildPathFromCurrentDir("repository");

    private final String entityDisplayName;
    private final Map<Field, Property> notNullFields;
    private final IdentifierGenerator<K> keyGenerator;
    private final FileRepositoryProvider<K, E> fileRepositoryProvider;

    private boolean isTransactionOpen;
    private Map<K, E> repositoryMap;
    private Map<K, E> repositoryMapSnapshot;

    protected AbstractFileRepositoryV2(Class<E> entityClass, IdentifierGenerator<K> keyGenerator, @Nullable Path rootPath) {
        this.keyGenerator = keyGenerator;
        this.entityDisplayName = resolveEntityName(entityClass);
        this.fileRepositoryProvider = new FileRepositoryProvider<>(this.getClass(), entityClass, rootPath);

        this.repositoryMap = initEntities();
        this.notNullFields = resolveNotNullFields(entityClass);

        this.repositoryMapSnapshot = null;
        this.isTransactionOpen = false;
    }

    @Override
    public E getById(K id) {
        return repositoryMap.get(id);
    }

    @Override
    public boolean existById(K id) {
        return repositoryMap.containsKey(id);
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
            fileRepositoryProvider.overwriteStorageFile(repositoryMap.values());
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
                fileRepositoryProvider.overwriteStorageFile(repositoryMap.values());
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
            fileRepositoryProvider.overwriteStorageFile(repositoryMap.values());

            this.isTransactionOpen = false;
            this.repositoryMapSnapshot = null;
            log.info("Transaction commited");
        }
    }

    @Override
    public void abortTransaction() {
        if (this.isTransactionOpen) {
            this.repositoryMap = new HashMap<>(this.repositoryMapSnapshot);
            fileRepositoryProvider.overwriteStorageFile(repositoryMap.values());

            this.isTransactionOpen = false;
            this.repositoryMapSnapshot = null;
            log.info("Transaction aborted");
        }
    }

    protected Map<K, E> getRepositoryMap() {
        return this.repositoryMap;
    }

    protected ConstraintException buildConstraintException(String message) {
        return new ConstraintException(this.entityDisplayName, message);
    }

    protected void compareEntities(E newEntity, E oldEntity) throws ConstraintException {
        // FOR OVERWRITE
    }

    private void checkConstrains(E entity) {
        checkNotNullFields(entity);
        this.repositoryMap.values()
            .stream()
            .filter(existedEntity -> !Objects.equals(existedEntity.getId(), entity.getId()))    // Remove same entity from checking
            .forEach(existedEntity -> compareEntities(entity, existedEntity));
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

    private Map<K, E> initEntities() {
        Map<K, E> entitiesMap = new LinkedHashMap<>();
        List<E> entitiesList = fileRepositoryProvider.readStorageFile();
        for (E entity : entitiesList) {
            entitiesMap.put(entity.getId(), entity);
        }
        return entitiesMap;
    }
}
