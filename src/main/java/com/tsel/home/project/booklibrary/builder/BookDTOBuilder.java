package com.tsel.home.project.booklibrary.builder;

import com.tsel.home.project.booklibrary.dto.BookDTO;
import javafx.scene.control.CheckBox;

public final class BookDTOBuilder {

    private String name;
    private String author;
    private String shelf;
    private String cycleName;
    private String cycleNumber;
    private CheckBox cycleEnded;
    private CheckBox read;
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

    public BookDTOBuilder shelf(String shelf) {
        this.shelf = shelf;
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

    public BookDTOBuilder pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    public BookDTO build() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setName(name);
        bookDTO.setAuthor(author);
        bookDTO.setShelf(shelf);
        bookDTO.setCycleName(cycleName);
        bookDTO.setCycleNumber(cycleNumber);
        bookDTO.setCycleEnded(cycleEnded);
        bookDTO.setRead(read);
        bookDTO.setPages(pages);
        return bookDTO;
    }
}
