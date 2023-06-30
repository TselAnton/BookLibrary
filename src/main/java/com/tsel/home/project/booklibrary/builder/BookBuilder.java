package com.tsel.home.project.booklibrary.builder;

import com.tsel.home.project.booklibrary.data.Book;

public final class BookBuilder {

    private String name;
    private String author;
    private String publisher;
    private Integer pages;
    private Integer bookshelf;
    private Boolean read;
    private Boolean autograph;
    private String cycleName;
    private Integer numberInSeries;
    private Double price;
    private String coverImgAbsolutePath;

    private BookBuilder() {}

    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public BookBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BookBuilder author(String author) {
        this.author = author;
        return this;
    }

    public BookBuilder publisher(String publisher) {
        this.publisher = publisher;
        return this;
    }

    public BookBuilder pages(Integer pages) {
        this.pages = pages;
        return this;
    }

    @Deprecated
    public BookBuilder bookshelf(Integer bookshelf) {
        this.bookshelf = bookshelf;
        return this;
    }

    public BookBuilder read(Boolean read) {
        this.read = read;
        return this;
    }

    public BookBuilder autograph(Boolean autograph) {
        this.autograph = autograph;
        return this;
    }

    public BookBuilder cycleName(String cycleName) {
        this.cycleName = cycleName;
        return this;
    }

    public BookBuilder numberInSeries(Integer numberInSeries) {
        this.numberInSeries = numberInSeries;
        return this;
    }

    public BookBuilder coverImgAbsolutePath(String coverImgAbsolutePath) {
        this.coverImgAbsolutePath = coverImgAbsolutePath;
        return this;
    }

    public BookBuilder price(Double price) {
        this.price = price;
        return this;
    }

    public Book build() {
        Book book = new Book();
        book.setName(name);
        book.setAuthor(author);
        book.setPublisher(publisher);
        book.setPages(pages);
        book.setBookshelf(bookshelf);
        book.setRead(read);
        book.setAutograph(autograph);
        book.setCycleName(cycleName);
        book.setNumberInSeries(numberInSeries);
        book.setCoverImgAbsolutePath(coverImgAbsolutePath);
        book.setPrice(price);
        return book;
    }
}
