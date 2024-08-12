package com.tsel.home.project.booklibrary.dao.repository;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import java.util.List;

@Deprecated(since = "4.0")
public interface FileRepositoryOld<E extends BaseEntity<?>> {

    E getByName(String key);

    List<E> getAll();

    void save(E entity);

    void delete(E entity);
}
