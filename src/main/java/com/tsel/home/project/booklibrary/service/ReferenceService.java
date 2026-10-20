package com.tsel.home.project.booklibrary.service;

import com.tsel.home.project.booklibrary.dao.data.Author;
import com.tsel.home.project.booklibrary.dao.data.BaseEntity;
import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.dao.data.Cycle;
import com.tsel.home.project.booklibrary.dao.data.Genre;
import com.tsel.home.project.booklibrary.dao.data.Publisher;
import com.tsel.home.project.booklibrary.dao.repository.FileRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.GenreRepository;
import com.tsel.home.project.booklibrary.dao.repository.impl.PublisherRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ReferenceService {

    private final BookRepository books;
    private final AuthorRepository authors;
    private final CycleRepository cycles;
    private final GenreRepository genres;
    private final PublisherRepository publishers;

    public ReferenceService(
        BookRepository books,
        AuthorRepository authors,
        CycleRepository cycles,
        GenreRepository genres,
        PublisherRepository publishers
    ) {
        this.books = books;
        this.authors = authors;
        this.cycles = cycles;
        this.genres = genres;
        this.publishers = publishers;
    }

    public List<Entry> entries(ReferenceType type) {
        Map<UUID, Long> counts = books.getAll().stream()
            .map(type::referenceId)
            .filter(Objects::nonNull)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return repository(type).getAll().stream()
            .map(entity -> entry(entity, counts.getOrDefault(entity.getId(), 0L)))
            .sorted(Comparator.comparing(Entry::name, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public List<Book> linkedBooks(ReferenceType type, UUID id) {
        return books.getAll().stream()
            .filter(book -> Objects.equals(type.referenceId(book), id))
            .sorted(Comparator.comparing(Book::getName, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    public void save(ReferenceType type, UUID id, String name, Boolean ended, Integer size) {
        requireExisting(type, id);
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Введите название записи.");
        }
        if (entries(type).stream().anyMatch(value -> !value.id().equals(id)
            && normalize(value.name()).equals(normalize(trimmed)))) {
            throw new IllegalArgumentException("Запись с таким названием уже существует.");
        }
        if (type == ReferenceType.CYCLES && (size == null || size < 0 || ended == null)) {
            throw new IllegalArgumentException("Число книг в цикле должно быть целым и не меньше 0.");
        }

        FileRepository<UUID, ? extends BaseEntity<UUID>> repository = repository(type);
        repository.beginTransaction();
        try {
            switch (type) {
                case AUTHORS -> authors.save(new Author(id, trimmed));
                case CYCLES -> cycles.save(new Cycle(id, trimmed, ended, size));
                case GENRES -> genres.save(new Genre(id, trimmed));
                case PUBLISHERS -> publishers.save(new Publisher(id, trimmed));
            }
            repository.commitTransaction();
        } catch (RuntimeException e) {
            rollback(repository, e);
            throw e;
        }
    }

    public void delete(ReferenceType type, UUID id) {
        requireExisting(type, id);
        int count = linkedBooks(type, id).size();
        if (count > 0) {
            throw new IllegalStateException("Удаление недоступно: связанных книг — " + count + ".");
        }
        FileRepository<UUID, ? extends BaseEntity<UUID>> repository = repository(type);
        repository.beginTransaction();
        try {
            repository.deleteById(id);
            repository.commitTransaction();
        } catch (RuntimeException e) {
            rollback(repository, e);
            throw e;
        }
    }

    private void requireExisting(ReferenceType type, UUID id) {
        if (id == null || !repository(type).existById(id)) {
            throw new IllegalStateException("Запись больше не существует. Откройте справочник заново.");
        }
    }

    private FileRepository<UUID, ? extends BaseEntity<UUID>> repository(ReferenceType type) {
        return switch (type) {
            case AUTHORS -> authors;
            case CYCLES -> cycles;
            case GENRES -> genres;
            case PUBLISHERS -> publishers;
        };
    }

    private Entry entry(BaseEntity<UUID> entity, long count) {
        if (entity instanceof Author author) {
            return new Entry(author.getId(), author.getName(), count, null, null);
        }
        if (entity instanceof Cycle cycle) {
            return new Entry(cycle.getId(), cycle.getName(), count, cycle.getEnded(), cycle.getBooksInCycle());
        }
        if (entity instanceof Genre genre) {
            return new Entry(genre.getId(), genre.getName(), count, null, null);
        }
        Publisher publisher = (Publisher) entity;
        return new Entry(publisher.getId(), publisher.getName(), count, null, null);
    }

    private String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT).replace('ё', 'е');
    }

    private void rollback(FileRepository<UUID, ? extends BaseEntity<UUID>> repository, RuntimeException original) {
        try {
            repository.abortTransaction();
        } catch (RuntimeException rollbackError) {
            original.addSuppressed(rollbackError);
        }
    }

    public record Entry(UUID id, String name, long bookCount, Boolean ended, Integer size) {}
}
