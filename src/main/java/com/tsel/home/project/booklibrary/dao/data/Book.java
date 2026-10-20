package com.tsel.home.project.booklibrary.dao.data;

import com.tsel.home.project.booklibrary.dao.annotation.Property;
import com.tsel.home.project.booklibrary.dao.schema.CurrentTimeDefault;
import com.tsel.home.project.booklibrary.dao.schema.NullPolicy;
import com.tsel.home.project.booklibrary.dao.schema.StorageEntity;
import com.tsel.home.project.booklibrary.dao.schema.StorageField;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
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
@EqualsAndHashCode(exclude = {"coverImgAbsolutePath"})
@StorageEntity(value = "book", displayName = "Книга", version = 1)
public class Book implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    @Property(value = "идентификатор")
    private UUID id;

    @Property(value = "название", nullable = false)
    private String name;

    @Property(value = "жанр книги")
    private UUID genreId;

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

    @Property(value = "год издания", nullable = false)
    @StorageField(defaultJson = "0", onNull = NullPolicy.USE_DEFAULT)
    private Integer publicationYear;

    @Property(value = "аудиокниги")
    @StorageField(defaultJson = "[]", onNull = NullPolicy.USE_DEFAULT)
    private List<UUID> audioBookSiteIds = new ArrayList<>();

    @Property(value = "время создания", nullable = false)
    @StorageField(provider = CurrentTimeDefault.class, onNull = NullPolicy.USE_DEFAULT)
    private Long createdAt;

    @Property(value = "время обновления", nullable = false)
    @StorageField(provider = CurrentTimeDefault.class, onNull = NullPolicy.USE_DEFAULT)
    private Long updatedAt;

    @Property(value = "время обновления флага 'прочитано'")
    private Long readUpdatedAt;
}
