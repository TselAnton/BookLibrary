package com.tsel.home.project.booklibrary.dao.repository;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import java.io.Serializable;
import java.util.List;

public interface FileRepository<K extends Serializable, E extends BaseEntity<K>> {

    E getById(K id);

    List<E> getAll();

    void save(E entity);

    void delete(E entity);

    void deleteById(K id);
}
