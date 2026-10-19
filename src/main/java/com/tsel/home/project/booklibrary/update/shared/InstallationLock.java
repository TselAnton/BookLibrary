package com.tsel.home.project.booklibrary.update.shared;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public final class InstallationLock implements AutoCloseable {

    private final FileChannel channel;
    private final FileLock lock;

    private InstallationLock(FileChannel channel, FileLock lock) {
        this.channel = channel;
        this.lock = lock;
    }

    public static InstallationLock acquire(Path directory) throws IOException {
        FileChannel channel = FileChannel.open(directory.resolve(".booklibrary.lock"),
            StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        try {
            FileLock lock = channel.tryLock();
            if (lock == null) {
                throw new IOException("Библиотека уже открыта или обновляется. Закройте другой экземпляр приложения.");
            }
            return new InstallationLock(channel, lock);
        } catch (IOException | OverlappingFileLockException e) {
            channel.close();
            throw new IOException("Не удалось заблокировать библиотеку: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws IOException {
        try {
            lock.release();
        } finally {
            channel.close();
        }
    }
}
