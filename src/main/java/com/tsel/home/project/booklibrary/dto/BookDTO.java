package com.tsel.home.project.booklibrary.dto;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;

import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.search.SearchField;
import java.util.List;
import java.util.UUID;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "cover")
@EqualsAndHashCode(exclude = "cover")
public class BookDTO {

    private UUID id;

    @SearchField(description = "Поиск по названию")
    private String name;

    private ImageView cover;

    @SearchField(aliases = {"image", "картинка"}, description = "Поиск по наличию картинки для обложки")
    private Boolean hasCoverImage;

    @SearchField(aliases = {"author", "автор"}, description = "Поиск по автору")
    private String author;

    @SearchField(aliases = {"publisher", "издатель"}, description = "Поиск по издателю")
    private String publisher;

    @SearchField(aliases = {"year", "год"}, description = "Поиск по году публикации")
    private Integer publicationYear;

    @SearchField(description = "Поиск по названию цикла")
    private String cycleName;

    private String cycleNumber;

    @SearchField(aliases = {"cycle", "цикл"}, description = "Поиск по законченным циклам")
    private CheckBox cycleEnded;

    @SearchField(aliases = {"genre", "жанр"}, description = "Поиск по названию жанра")
    private String genre;

    @SearchField(aliases = {"read", "прочитано"}, description = "Поиск по прочитанному")
    private CheckBox read;

    @SearchField(aliases = {"sign", "автограф"}, description = "Поиск по наличию автографа")
    private CheckBox autograph;

    @SearchField(aliases = {"pages", "страницы"}, description = "Поиск по количеству страниц")
    private Integer pages;

    @SearchField(aliases = {"price", "цена"}, description = "Поиск по цене")
    private Double price;

    @SearchField(aliases = {"hard", "твердая"}, description = "Поиск по наличию твердой обложки")
    private CheckBox hardCover;

    @SearchField(aliases = {"audiobook", "аудиокнига"}, description = "Поиск по наличию аудиокниги на любом сайте")
    private CheckBox hasAnyAudioBookSite;

    private List<String> audiobookSites;

    public static class BookDTOBuilder {

        @SuppressWarnings("FieldCanBeLocal")
        private ImageView cover;

        public BookDTOBuilder cover(Book book) {
            ImageProvider imageProvider = getBean(ImageProvider.class);
            ImageProvider.ImageHolder imageHolder = imageProvider.resolveSmallCover(book);

            this.cover = new ImageView(imageHolder.image());
            this.hasCoverImage = !imageHolder.isDefault();
            return this;
        }
    }
}
