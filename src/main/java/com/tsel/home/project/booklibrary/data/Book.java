package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
@EqualsAndHashCode(exclude = "coverImgAbsolutePath")
public class Book implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    private UUID id = UUID.randomUUID();
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

    @Override
    public String getKey() {
        return id.toString();
    }
}
