package com.tsel.home.project.booklibrary.javafx;

import com.tsel.home.project.booklibrary.dao.data.*;
import com.tsel.home.project.booklibrary.dao.repository.impl.*;
import com.tsel.home.project.booklibrary.helper.*;
import com.tsel.home.project.booklibrary.search.SearchService;
import com.tsel.home.project.booklibrary.utils.table.TableScroll;
import java.nio.file.Path;
import java.util.UUID;

/** Isolated catalog for visual verification; never reads the user's repository. */
final class DesignFixture {
    private static final java.util.Map<Class<?>, Object> previous = new java.util.HashMap<>();
    static void initialize(Path directory) {
        for (Class<?> type : java.util.List.of(PropertyProvider.class, AudioBookSiteRepository.class, AuthorRepository.class,
            PublisherRepository.class, CycleRepository.class, GenreRepository.class, UserSettingsRepository.class,
            BookRepository.class, ImageProvider.class, SearchService.class, TableScroll.class, DateProvider.class))
            previous.put(type, SimpleApplicationContext.getBean(type));
        SimpleApplicationContext.replaceBean(PropertyProvider.class, () -> new PropertyProvider(directory));
        SimpleApplicationContext.replaceBean(AudioBookSiteRepository.class, () -> new AudioBookSiteRepository(directory));
        SimpleApplicationContext.replaceBean(AuthorRepository.class, () -> new AuthorRepository(directory));
        SimpleApplicationContext.replaceBean(PublisherRepository.class, () -> new PublisherRepository(directory));
        SimpleApplicationContext.replaceBean(CycleRepository.class, () -> new CycleRepository(directory));
        SimpleApplicationContext.replaceBean(GenreRepository.class, () -> new GenreRepository(directory));
        SimpleApplicationContext.replaceBean(UserSettingsRepository.class, () -> new UserSettingsRepository(directory));
        SimpleApplicationContext.replaceBean(BookRepository.class, () -> new BookRepository(directory));
        SimpleApplicationContext.replaceBean(ImageProvider.class, ImageProvider::new);
        SimpleApplicationContext.replaceBean(SearchService.class, SearchService::new);
        SimpleApplicationContext.replaceBean(TableScroll.class, TableScroll::new);
        SimpleApplicationContext.replaceBean(DateProvider.class, DateProvider::new);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static void restore() {
        SimpleApplicationContext.getBean(ImageProvider.class).close();
        previous.forEach((type, bean) -> SimpleApplicationContext.replaceBean((Class) type, () -> bean));
        previous.clear();
    }

    static UUID populate() {
        AuthorRepository authors = SimpleApplicationContext.getBean(AuthorRepository.class);
        PublisherRepository publishers = SimpleApplicationContext.getBean(PublisherRepository.class);
        CycleRepository cycles = SimpleApplicationContext.getBean(CycleRepository.class);
        GenreRepository genres = SimpleApplicationContext.getBean(GenreRepository.class);
        BookRepository books = SimpleApplicationContext.getBean(BookRepository.class);
        UUID author = authors.save(new Author(null, "Фрэнк Герберт"));
        UUID longAuthor = authors.save(new Author(null,
            "Очень длинное имя автора для проверки ширины выпадающего списка и полного текста подсказки ".repeat(3)));
        UUID publisher = publishers.save(new Publisher(null, "АСТ"));
        UUID cycle = cycles.save(new Cycle(null, "Хроники Дюны", true, 6));
        UUID genre = genres.save(new Genre(null, "Фантастика"));
        UUID first = null;
        String[] names = {"Дюна", "Мессия Дюны", "Марсианин", "Солярис", "Хоббит", "1984",
            "Очень длинное название книги для проверки обрезки кириллического текста в таблице"};
        for (int i = 0; i < names.length; i++) {
            UUID id = books.save(Book.builder().name(names[i]).authorId(i == names.length - 1 ? longAuthor : author).publisherId(publisher)
                .genreId(genre).cycleId(i < 2 ? cycle : null).numberInSeries(i < 2 ? i + 1 : null)
                .pages(704 - i * 48).publicationYear(1965 + i).price(1250.50 + i)
                .read(i == 4).hardCover(true).autograph(false)
                .coverImgAbsolutePath("Z:/missing-cover.png").build());
            if (first == null) first = id;
        }
        return first;
    }
}
