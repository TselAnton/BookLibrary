package com.tsel.home.project.booklibrary.repository;

import com.tsel.home.project.booklibrary.data.BaseEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static java.lang.String.format;

public abstract class AbstractFileRepository<E extends BaseEntity> implements FileRepository<E> {

    protected final String storageFileName;
    protected final LinkedHashMap<String, E> repositoryMap;

    protected AbstractFileRepository(String storageFileName) {
        this.storageFileName = storageFileName;

        if (isStorageAlreadyExist()) {
            try {
                repositoryMap = readStorageFile();
                updateNewFields();
                updateStorageKeys();
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(format("Problem while reading file: %s", storageFileName), e);
            }

        } else {
            try {
                createNewStorageFile();
                repositoryMap = new LinkedHashMap<>();
            } catch (IOException e) {
                throw new IllegalStateException(format("Problem while creating file: %s", storageFileName), e);
            }
        }
    }

    private boolean isStorageAlreadyExist() {
        Path path = Paths.get(storageFileName);
        return Files.exists(path);
    }

    private void createNewStorageFile() throws IOException {
        Path path = Paths.get(storageFileName);
        Files.createFile(path);
    }

    @SuppressWarnings("unchecked")
    private LinkedHashMap<String, E> readStorageFile() throws FileNotFoundException {
        try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(storageFileName))) {
            return (LinkedHashMap<String, E>) inputStream.readObject();

        } catch (EOFException e) {
            return new LinkedHashMap<>();
        } catch (IOException e) {
            throw new IllegalStateException(format("Problem while reading file: %s", storageFileName), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(format("Cant cast object from: %s", storageFileName), e);
        }
    }

    private void updateStorageKeys() {
        LinkedHashMap<String, E> updatedRepository = new LinkedHashMap<>();

        Iterator<Map.Entry<String, E>> iterator = repositoryMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, E> entry = iterator.next();
            updatedRepository.put(entry.getValue().getKey().toLowerCase(Locale.ROOT), entry.getValue());
            iterator.remove();
        }

        repositoryMap.putAll(updatedRepository);
    }

    protected void updateStorageFile() {
        try (ObjectOutputStream inputStream = new ObjectOutputStream(new FileOutputStream(storageFileName))) {
            inputStream.writeObject(repositoryMap);
            inputStream.flush();

        } catch (IOException e) {
            throw new IllegalStateException(format("Problem while writing in the file: %s", storageFileName), e);
        }
    }

    protected void updateNewFields() {}

    @Override
    public E getByName(String compositeKey) {
        return repositoryMap.get(compositeKey.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<E> getAll() {
        return new ArrayList<>(repositoryMap.values());
    }

    @Override
    public E save(E entity) {
        repositoryMap.put(entity.getKey().toLowerCase(Locale.ROOT), entity);
        updateStorageFile();
        return entity;
    }

    @Override
    public void delete(E entity) {
        repositoryMap.remove(entity.getKey().toLowerCase(Locale.ROOT));
        updateStorageFile();
    }
}
