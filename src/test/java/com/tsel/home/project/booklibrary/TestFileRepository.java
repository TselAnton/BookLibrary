package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import java.io.Serializable;
import java.util.List;
import javax.annotation.Nullable;

public abstract class TestFileRepository<T extends Serializable> implements FileRepository<T, BaseEntity<T>> {

    @Nullable
    @Override
    public BaseEntity<T> getById(T id) {
        // ONLY FOR TESTS
        return null;
    }

    @Override
    public boolean existById(T id) {
        // ONLY FOR TESTS
        return false;
    }

    @Override
    public List<BaseEntity<T>> getAll() {
        // ONLY FOR TESTS
        return List.of();
    }

    @Override
    public T save(BaseEntity<T> entity) {
        // ONLY FOR TESTS
        return null;
    }

    @Override
    public void delete(BaseEntity<T> entity) {
        // ONLY FOR TESTS
    }

    @Override
    public void deleteById(T id) {
        // ONLY FOR TESTS
    }

    @Override
    public void beginTransaction() {
        // ONLY FOR TESTS
    }

    @Override
    public void commitTransaction() {
        // ONLY FOR TESTS
    }

    @Override
    public void abortTransaction() {
        // ONLY FOR TESTS
    }
}
