package com.tsel.home.project.booklibrary.dao.repository;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

public interface FileRepository<K extends Serializable, E extends BaseEntity<K>> {

    @Nullable
    E getById(K id);

    boolean existById(K id);

    List<E> getAll();

    K save(E entity);

    void delete(E entity);

    void deleteById(K id);

    void beginTransaction();

    void commitTransaction();

    void abortTransaction();
}
