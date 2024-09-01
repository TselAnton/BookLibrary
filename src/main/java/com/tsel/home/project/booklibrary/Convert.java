package com.tsel.home.project.booklibrary;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.tsel.home.project.booklibrary.data.AudioBookSite;
import com.tsel.home.project.booklibrary.helper.FileRepositoryProvider;
import com.tsel.home.project.booklibrary.repository.impl.AudioBookSiteRepository;
import com.tsel.home.project.booklibrary.repository.impl.AuthorRepository;
import com.tsel.home.project.booklibrary.repository.impl.BookRepository;
import com.tsel.home.project.booklibrary.repository.impl.CycleRepository;
import com.tsel.home.project.booklibrary.repository.impl.PublisherRepository;
import com.tsel.home.project.booklibrary.repository.impl.UserSettingsRepository;
import com.tsel.home.project.booklibrary.utils.FileUtils;
import com.tsel.home.project.booklibrary.utils.MyGson;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class Convert {

    private static final Path AUDIO_BOOK_SITE_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\audioBookSiteStorage.json");
    private static final Path AUTHOR_SITE_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\authorStorage.json");
    private static final Path PUBLISHER_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\publisherStorage.json");
    private static final Path CYCLE_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\bookCycleStorage.json");
    private static final Path USER_SETTINGS_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\userSettingsStorage.json");
    private static final Path BOOK_PATH = FileUtils.buildPathFromCurrentDir("\\converted\\bookStorage.json");

    private static final AudioBookSiteRepository AUDIO_BOOK_SITE_REPOSITORY = AudioBookSiteRepository.getInstance();
    private static final AuthorRepository AUTHOR_REPOSITORY = AuthorRepository.getInstance();
    private static final BookRepository BOOK_REPOSITORY = BookRepository.getInstance();
    private static final CycleRepository CYCLE_REPOSITORY = CycleRepository.getInstance();
    private static final PublisherRepository PUBLISHER_REPOSITORY = PublisherRepository.getInstance();
    private static final UserSettingsRepository USER_SETTINGS_REPOSITORY = UserSettingsRepository.getInstance();

    private static final Gson GSON = MyGson.buildGson();

    public static void main(String[] args) {
        List<Map<String, Object>> audioBooks = addId(convertToMap(AUDIO_BOOK_SITE_REPOSITORY.getAll())).toList();
        List<Map<String, Object>> authors = addId(convertToMap(AUTHOR_REPOSITORY.getAll())).toList();
        List<Map<String, Object>> cycles = addId(convertToMap(CYCLE_REPOSITORY.getAll())).toList();
        List<Map<String, Object>> publishers = addId(convertToMap(PUBLISHER_REPOSITORY.getAll())).toList();
        List<Map<String, Object>> userSettings = addId(convertToMap(USER_SETTINGS_REPOSITORY.getAll())).toList();
        List<Map<String, Object>> books = doBook(addId(convertToMap(BOOK_REPOSITORY.getAll())), authors, audioBooks, cycles, publishers);

        FileRepositoryProvider.overwriteStorageFile(audioBooks, AUDIO_BOOK_SITE_PATH);
        FileRepositoryProvider.overwriteStorageFile(authors, AUTHOR_SITE_PATH);
        FileRepositoryProvider.overwriteStorageFile(publishers, PUBLISHER_PATH);
        FileRepositoryProvider.overwriteStorageFile(cycles, CYCLE_PATH);
        FileRepositoryProvider.overwriteStorageFile(userSettings, USER_SETTINGS_PATH);
        FileRepositoryProvider.overwriteStorageFile(books, BOOK_PATH);

    }

    private static <E> Stream<Map<String, Object>> convertToMap(List<E> entities) {
        return entities.stream().map(e -> GSON.fromJson(GSON.toJson(e), new TypeToken<Map<String, Object>>(){}));
    }

    private static Stream<Map<String, Object>> addId(Stream<Map<String, Object>> stream) {
        return stream.map(e -> {
            e.put("id", UUID.randomUUID());
            return e;
        });
    }

    private static List<Map<String, Object>> doBook(
        Stream<Map<String, Object>> stream,
        List<Map<String, Object>> authors,
        List<Map<String, Object>> audioBooks,
        List<Map<String, Object>> cycles,
        List<Map<String, Object>> publishers) {

        return stream
            .map(b -> {
                String authorName = (String) b.get("author");
                b.remove("author");
                b.put("authorId", authors.stream().filter(a -> a.get("name").equals(authorName)).findFirst().map(a -> (UUID)a.get("id")).orElse(null));

                String publisherName = (String) b.get("publisher");
                b.remove("publisher");
                b.put("publisherId", publishers.stream().filter(p -> p.get("name").equals(publisherName)).findFirst().map(p -> (UUID)p.get("id")).orElse(null));

                String cycleName = (String) b.get("cycleName");
                if (cycleName != null) {
                    b.remove("cycleName");
                    b.put("cycleId", cycles.stream().filter(c -> c.get("name").equals(cycleName)).findFirst().map(c -> (UUID)c.get("id")).orElse(null));
                }

                List<String> audiobookSites = (List<String>) b.get("audiobookSites");
                if (audiobookSites == null && audiobookSites.isEmpty()) {
                    b.remove("audiobookSites");
                    b.put("audioBookSiteIds", new ArrayList<UUID>());
                } else {
                    List<UUID> ids = new ArrayList<>();
                    audiobookSites.forEach(audiobookSite -> {
                        ids.add(audioBooks.stream().filter(a -> a.get("name").equals(audiobookSite)).findFirst().map(p -> (UUID)p.get("id")).orElse(null));
                    });
                    b.remove("audiobookSites");
                    b.put("audioBookSiteIds", ids);
                }

                return b;
            })
            .toList();
    }
}
