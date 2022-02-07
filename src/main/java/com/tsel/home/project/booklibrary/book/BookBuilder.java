package com.tsel.home.project.booklibrary.book;

public final class BookBuilder {
    private String name;
    private String author;
    private String publisher;
    private int pages;
    private int bookshelf;
    private boolean read;
    private boolean fullSeries;

    private BookBuilder() {
    }

    public static BookBuilder buildBook() {
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

    public BookBuilder pages(int pages) {
        this.pages = pages;
        return this;
    }

    public BookBuilder bookshelf(int bookshelf) {
        this.bookshelf = bookshelf;
        return this;
    }

    public BookBuilder read(boolean read) {
        this.read = read;
        return this;
    }

    public BookBuilder fullSeries(boolean fullSeries) {
        this.fullSeries = fullSeries;
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
        book.setFullSeries(fullSeries);
        return book;
    }
}
