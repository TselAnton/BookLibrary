package com.tsel.home.project.booklibrary.builder;

import com.tsel.home.project.booklibrary.data.Book;
import com.tsel.home.project.booklibrary.dto.BookDTO;
import com.tsel.home.project.booklibrary.utils.ImageProvider;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;

public final class BookDTOBuilder {

    private static final ImageProvider IMAGE_PROVIDER = ImageProvider.getInstance();

    private String name;
    private ImageView cover;
    private String author;
    private String publisher;
    private String cycleName;
    private String cycleNumber;
    private CheckBox cycleEnded;
    private CheckBox read;
    private CheckBox autograph;
    private Integer pages;
    private Double price;
    private CheckBox hardCover;

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

    public BookDTOBuilder autograph(CheckBox autograph) {
        this.autograph = autograph;
        return this;
    }

    public BookDTOBuilder pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    public BookDTOBuilder cover(Book book) {
        this.cover = new ImageView(IMAGE_PROVIDER.resolveSmallCover(book));
        return this;
    }

    public BookDTOBuilder price(Double price) {
        this.price = price;
        return this;
    }

    public BookDTOBuilder hardCover(CheckBox hardCover) {
        this.hardCover = hardCover;
        return this;
    }

    public BookDTO build() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setCover(cover);
        bookDTO.setName(name);
        bookDTO.setAuthor(author);
        bookDTO.setPublisher(publisher);
        bookDTO.setCycleName(cycleName);
        bookDTO.setCycleNumber(cycleNumber);
        bookDTO.setCycleEnded(cycleEnded);
        bookDTO.setRead(read);
        bookDTO.setAutograph(autograph);
        bookDTO.setPages(pages);
        bookDTO.setPrice(price);
        bookDTO.setHardCover(hardCover);
        return bookDTO;
    }
}
