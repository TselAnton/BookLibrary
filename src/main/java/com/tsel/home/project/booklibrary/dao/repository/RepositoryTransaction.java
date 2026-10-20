package com.tsel.home.project.booklibrary.dao.repository;

import com.tsel.home.project.booklibrary.helper.StorageWriteBatch;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/** Общая транзакция репозиториев в потоке вызывающего сервиса. */
public final class RepositoryTransaction {

    private RepositoryTransaction() {}

    public static <T> T execute(Supplier<T> action, AbstractFileRepository<?, ?>... repositories) {
        return execute(new StorageWriteBatch(), action, repositories);
    }

    static <T> T execute(StorageWriteBatch batch, Supplier<T> action, AbstractFileRepository<?, ?>... repositories) {
        List<AbstractFileRepository<?, ?>> started = new ArrayList<>();
        try {
            for (AbstractFileRepository<?, ?> repository : Arrays.stream(repositories).distinct().toList()) {
                repository.beginTransaction();
                started.add(repository);
            }
            T result = action.get();
            for (AbstractFileRepository<?, ?> repository : started) {
                repository.prepareCommit(batch);
            }
            batch.commit();
            started.forEach(AbstractFileRepository::finishCommit);
            return result;
        } catch (RuntimeException e) {
            started.forEach(AbstractFileRepository::abortTransaction);
            throw e;
        }
    }
}
