package com.tsel.home.project.booklibrary.dto;

import static com.tsel.home.project.booklibrary.helper.SimpleApplicationContext.getBean;

import com.tsel.home.project.booklibrary.dao.data.Book;
import com.tsel.home.project.booklibrary.filter.annotation.FilterProperty;
import com.tsel.home.project.booklibrary.filter.provider.impl.CycleStatusValueProvider;
import com.tsel.home.project.booklibrary.helper.ImageProvider;
import com.tsel.home.project.booklibrary.ui.BookPriceFormatter;
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

    @FilterProperty(label = "Название", order = 10, allowEmpty = false)
    private String name;

    private ImageView cover;

    @FilterProperty(label = "Изображение обложки", order = 120, allowEmpty = false)
    private Boolean hasCoverImage;

    @FilterProperty(label = "Автор", order = 20, editor = "choices", allowEmpty = false)
    private String author;

    @FilterProperty(label = "Издатель", order = 30, editor = "choices", allowEmpty = false)
    private String publisher;

    @FilterProperty(label = "Год издания", order = 50, allowEmpty = false)
    private Integer publicationYear;

    @FilterProperty(label = "Цикл", order = 100, editor = "choices", emptyLabel = "Без цикла")
    private String cycleName;

    private String cycleNumber;

    @FilterProperty(
        label = "Завершённость цикла",
        order = 110,
        trueLabel = "Завершён",
        falseLabel = "Продолжается",
        emptyLabel = "Завершённость не указана",
        notApplicableLabel = "Без цикла",
        valueProvider = CycleStatusValueProvider.class
    )
    private CheckBox cycleEnded;

    @FilterProperty(label = "Жанр", order = 90, editor = "choices", emptyLabel = "Без жанра")
    private String genre;

    @FilterProperty(
        label = "Прочтение", order = 70,
        trueLabel = "Прочитано", falseLabel = "Не прочитано", allowEmpty = false
    )
    private CheckBox read;

    @FilterProperty(label = "Автограф", order = 80, allowEmpty = false)
    private CheckBox autograph;

    @FilterProperty(label = "Количество страниц", order = 40, allowEmpty = false)
    private Integer pages;

    @FilterProperty(
        label = "Стоимость", order = 130, unit = "руб.",
        emptyLabel = BookPriceFormatter.PRICELESS, notEmptyLabel = "Стоимость указана"
    )
    private Double price;

    @FilterProperty(label = "Твёрдая обложка", order = 60, allowEmpty = false)
    private CheckBox hardCover;

    @FilterProperty(label = "Аудиокнига", visible = false)
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
