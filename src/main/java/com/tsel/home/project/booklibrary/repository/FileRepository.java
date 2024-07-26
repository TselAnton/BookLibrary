package com.tsel.home.project.booklibrary.repository;

import com.tsel.home.project.booklibrary.data.BaseEntity;
import java.io.Serializable;
import java.util.List;

public interface FileRepository<K extends Serializable, E extends BaseEntity<K>> {

    E getByName(K key);

    List<E> getAll();

    void save(E entity);

    void delete(E entity);
}
