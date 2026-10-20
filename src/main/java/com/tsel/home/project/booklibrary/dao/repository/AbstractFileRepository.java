package com.tsel.home.project.booklibrary.dao.repository;

import static com.tsel.home.project.booklibrary.utils.FileUtils.buildPathFromCurrentDir;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.google.gson.Gson;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.exception.ConstraintException;
import com.tsel.home.project.booklibrary.dao.exception.NotNullConstraintException;
import com.tsel.home.project.booklibrary.dao.identifier.IdentifierGenerator;
import com.tsel.home.project.booklibrary.dao.schema.EntitySchema;
import com.tsel.home.project.booklibrary.dao.schema.StorageEntity;
import com.tsel.home.project.booklibrary.helper.FileRepositoryProvider;
import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import com.tsel.home.project.booklibrary.utils.MyGson;
import com.tsel.home.project.booklibrary.utils.StringUtils;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.time.Clock;
import java.util.function.BiConsumer;
import java.util.*;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractFileRepository<K extends Serializable, E extends BaseEntity<K>> implements FileRepository<K, E> {

    public static final Path DEFAULT_REPOSITORY_PATH = buildPathFromCurrentDir("repository");

    protected static final Gson GSON = MyGson.buildGson();

    private final Class<E> entityClass;
    private final EntitySchema schema;
    private final String entityDisplayName;
    private final Map<Field, Property> notNullFields;
    private final IdentifierGenerator<K> keyGenerator;
    private final FileRepositoryProvider<K, E> fileRepositoryProvider;

    private boolean isTransactionOpen;
    private Map<K, E> repositoryMap;
    private Map<K, E> repositoryMapSnapshot;

    protected AbstractFileRepository(Class<E> entityClass, IdentifierGenerator<K> keyGenerator, @Nullable Path rootPath) {
        this.entityClass = entityClass;
        this.schema = entityClass.isAnnotationPresent(StorageEntity.class) ? new EntitySchema(entityClass) : null;
        this.keyGenerator = keyGenerator;
        this.entityDisplayName = resolveEntityName(entityClass);
        this.fileRepositoryProvider = new FileRepositoryProvider<>(this.getClass(), entityClass, rootPath);

        this.notNullFields = resolveNotNullFields(entityClass);
        this.repositoryMap = initEntities();

        this.repositoryMapSnapshot = null;
        this.isTransactionOpen = false;
    }

    @Override
    public E getById(K id) {
        return copy(repositoryMap.get(id));
    }

    @Override
    public boolean existById(K id) {
        return repositoryMap.containsKey(id);
    }

    @Override
    public List<E> getAll() {
        return repositoryMap.values().stream().map(this::copy).toList();
    }

    @Override
    public K save(E entity) {
        return save(entity, this::prepareSave);
    }

    protected K save(E entity, BiConsumer<E, E> preparation) {
        boolean ownTransaction = !isTransactionOpen;
        if (ownTransaction) {
            beginTransaction();
        }
        try {
            if (entity.getId() == null) {
                entity.setId(keyGenerator.generate());
            }
            E candidate = copy(entity);
            if (schema != null) {
                candidate = GSON.fromJson(schema.normalize(GSON.toJsonTree(candidate).getAsJsonObject(), storageClock()), entityClass);
            }
            preparation.accept(candidate, copy(repositoryMap.get(entity.getId())));
            checkConstrains(candidate);
            repositoryMap.put(candidate.getId(), candidate);
            if (ownTransaction) {
                commitTransaction();
            }
            return candidate.getId();
        } catch (RuntimeException e) {
            if (ownTransaction) {
                abortTransaction();
            }
            throw e;
        }
    }

    @Override
    public void delete(E entity) {
        this.deleteById(entity.getId());
    }

    @Override
    public void deleteById(K id) {
        boolean ownTransaction = !isTransactionOpen;
        if (ownTransaction) {
            beginTransaction();
        }
        try {
            repositoryMap.remove(id);
            if (ownTransaction) {
                commitTransaction();
            }
        } catch (RuntimeException e) {
            if (ownTransaction) {
                abortTransaction();
            }
            throw e;
        }
    }

    @Override
    public void beginTransaction() {
        if (isTransactionOpen) {
            throw new IllegalStateException("Транзакция уже открыта: " + entityDisplayName);
        }
        repositoryMapSnapshot = copyMap(repositoryMap);
        isTransactionOpen = true;
    }

    @Override
    public void commitTransaction() {
        if (isTransactionOpen) {
            try {
                StorageWriteBatch batch = new StorageWriteBatch();
                prepareCommit(batch);
                batch.commit();
                finishCommit();
            } catch (RuntimeException e) {
                abortTransaction();
                throw e;
            }
        }
    }

    @Override
    public void abortTransaction() {
        if (isTransactionOpen) {
            repositoryMap = repositoryMapSnapshot;
            finishCommit();
        }
    }

    void prepareCommit(StorageWriteBatch batch) {
        if (!isTransactionOpen) {
            throw new IllegalStateException("Транзакция не открыта: " + entityDisplayName);
        }
        if (!GSON.toJsonTree(repositoryMap).equals(GSON.toJsonTree(repositoryMapSnapshot))) {
            fileRepositoryProvider.prepareWrite(batch, repositoryMap.values());
        }
    }

    void finishCommit() {
        isTransactionOpen = false;
        repositoryMapSnapshot = null;
    }

    protected Clock storageClock() {
        return Clock.systemUTC();
    }

    protected void prepareSave(E entity, E previous) {
        // FOR OVERWRITE
    }

    protected Map<K, E> getRepositoryMap() {
        return copyMap(repositoryMap);
    }

    private E copy(E entity) {
        return entity == null ? null : GSON.fromJson(GSON.toJsonTree(entity), entityClass);
    }

    private Map<K, E> copyMap(Map<K, E> source) {
        Map<K, E> result = new LinkedHashMap<>();
        source.forEach((key, entity) -> result.put(key, copy(entity)));
        return result;
    }

    protected ConstraintException buildConstraintException(String message) {
        return new ConstraintException(this.entityDisplayName, message.toLowerCase(Locale.ROOT));
    }

    /**
     * Сравнение сущностей при проверки ограничений
     * @param newEntity Сохраняемая сущность
     * @param oldEntity Существующая сущность
     * @throws ConstraintException Ошибкка в случае, если сохраняемая сущность нарушает ограничения и не может быть сохранена
     */
    protected void compareEntities(E newEntity, E oldEntity) throws ConstraintException {
        // FOR OVERWRITE
    }

    /**
     * Метод для пре-чтения сущность в репозиторий
     * @param entity Сущность
     */
    protected void preCreateEntity(E entity) {
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
                    throw new NotNullConstraintException(this.entityDisplayName, fieldAnnotation.value().toLowerCase(Locale.ROOT));
                }
            } catch (IllegalAccessException e) {
                throw new NotNullConstraintException(this.entityDisplayName, fieldAnnotation.value().toLowerCase(Locale.ROOT));
            }
        }
    }

    private String resolveEntityName(Class<E> entityClass) {
        return ofNullable(entityClass.getAnnotation(StorageEntity.class))
            .map(StorageEntity::displayName)
            .filter(StringUtils::isNotBlank)
            .orElseThrow(() -> new IllegalStateException(
                format("Не задан displayName в @StorageEntity для сущности %s", entityClass.getName()))
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
        List<E> entitiesList = ofNullable(fileRepositoryProvider.readStorageFile()).orElse(Collections.emptyList());
        for (E entity : entitiesList) {
            if (entity == null || entity.getId() == null || entitiesMap.containsKey(entity.getId())) {
                throw new IllegalStateException("Пустой или повторный идентификатор: " + entityDisplayName);
            }
            preCreateEntity(entity);
            entitiesMap.put(entity.getId(), entity);
        }
        return entitiesMap;
    }
}
