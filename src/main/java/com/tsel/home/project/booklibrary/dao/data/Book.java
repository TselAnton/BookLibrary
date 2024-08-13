package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.EntityDisplayName;
import com.tsel.home.project.booklibrary.dao.annotation.Property;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"coverImgAbsolutePath", "bookshelf"})
@EntityDisplayName("Книга")
//@UniqueConstraint(name = "unique_book_constraint", properties = {"name", "authorId", "publisherId", "cycleName", "numberInSeries"})
public class Book implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название", nullable = false)
    private String name;

    @Property(value = "автор", nullable = false)
    private UUID authorId;

    @Property(value = "публицист", nullable = false)
    private UUID publisherId;

    @Property(value = "страницы")
    private Integer pages;

    @Property(value = "прочитано")
    private Boolean read;

    @Property(value = "автограф")
    private Boolean autograph;

    @Property(value = "цикл")
    private UUID cycleId;

    @Property(value = "номер в цикле")
    private Integer numberInSeries;

    @Property(value = "картинка обложки")
    private String coverImgAbsolutePath;

    @Property(value = "твёрдая обложка")
    private Boolean hardCover;

    @Property(value = "цена")
    private Double price;

    @Property(value = "аудиокниги")
    private List<String> audiobookSites = new ArrayList<>();    // TODO: на UUID

    @Deprecated
    private String author;
    @Deprecated
    private Integer bookshelf;
    @Deprecated
    private String cycleName;
    @Deprecated
    private String publisher;

    @Override
    public String getKey() {
        StringBuilder compositeKey = new StringBuilder();
        compositeKey.append(name.replaceAll(" ", "_").toLowerCase(Locale.ROOT));
        compositeKey.append("_");
        compositeKey.append(author.replaceAll(" ", "_").toLowerCase(Locale.ROOT));
        compositeKey.append("_");
        compositeKey.append(publisher.replaceAll(" ", "_").toLowerCase(Locale.ROOT));

        if (cycleName != null) {
            compositeKey.append("_");
            compositeKey.append(cycleName.replaceAll(" ", "_").toLowerCase(Locale.ROOT));
        }

        if (numberInSeries != null) {
            compositeKey.append("_");
            compositeKey.append(numberInSeries);
        }

        return compositeKey.toString();
    }
}
