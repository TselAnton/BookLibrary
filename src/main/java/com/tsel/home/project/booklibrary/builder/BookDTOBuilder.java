package com.tsel.home.project.booklibrary.builder;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import javafx.scene.control.CheckBox;

public final class BookDTOBuilder {

    private String name;
    private String author;
    private String publisher;
    private String cycleName;
    private String cycleNumber;
    private CheckBox cycleEnded;
    private CheckBox read;
    private boolean autograph;
    private Integer pages;

    private BookDTOBuilder() {}

    public static BookDTOBuilder builder() {
        return new BookDTOBuilder();
    }

    public BookDTOBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BookDTOBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookDTOBuilder publisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    public BookDTOBuilder cycleName(String cycleName) {
        this.cycleName = cycleName;
        return this;
    }

    public BookDTOBuilder cycleNumber(String cycleNumber) {
        this.cycleNumber = cycleNumber;
        return this;
    }

    public BookDTOBuilder cycleEnded(CheckBox cycleEnded) {
        this.cycleEnded = cycleEnded;
        return this;
    }

    public BookDTOBuilder read(CheckBox read) {
        this.read = read;
        return this;
    }

    public BookDTOBuilder autograph(Boolean autograph) {
        this.autograph = Boolean.TRUE.equals(autograph);
        return this;
    }

    public BookDTOBuilder pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    public BookDTO build() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(name);
        bookDTO.setAuthor(author);
        bookDTO.setPublisher(publisher);
        bookDTO.setCycleName(cycleName);
        bookDTO.setCycleNumber(cycleNumber);
        bookDTO.setCycleEnded(cycleEnded);
        bookDTO.setRead(read);
        bookDTO.setAutograph(autograph);
        bookDTO.setPages(pages);
        return bookDTO;
    }
}
