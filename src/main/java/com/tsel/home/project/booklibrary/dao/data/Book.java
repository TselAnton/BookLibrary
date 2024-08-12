package com.tsel.home.project.booklibrary.dao.data;

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
public class Book implements BaseEntity<UUID> {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    private UUID id;
    private String name;
    private UUID authorId;
    private UUID publisherId;
    private Integer pages;
    private Boolean read;
    private Boolean autograph;
    private UUID cycleId;
    private Integer numberInSeries;
    private String coverImgAbsolutePath;
    private Boolean hardCover;
    private Double price;
    private List<String> audiobookSites = new ArrayList<>();

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

    @Override
    public String getEntityPrintName() {
        return "Книга";
    }
}
