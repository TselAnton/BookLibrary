package com.tsel.home.project.booklibrary.repository;

import com.tsel.home.project.booklibrary.data.BaseEntity;

import java.util.List;

public interface FileRepository<E extends BaseEntity> {

    E getByKey(String id);

    List<E> getAll();

    void save(E entity);

    void delete(E entity);
}
