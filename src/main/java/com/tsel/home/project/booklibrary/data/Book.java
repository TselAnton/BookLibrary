package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"coverImgAbsolutePath", "bookshelf"})
public class Book implements BaseEntity<String> {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    private String name;
    private String author;
    private String publisher;
    private Integer pages;
    private Boolean read;
    private Boolean autograph;
    private String cycleName;
    private Integer numberInSeries;
    private String coverImgAbsolutePath;
    private Boolean hardCover;
    private Double price;
    private List<String> audiobookSites = new ArrayList<>();

    @Deprecated
    private Integer bookshelf;

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
