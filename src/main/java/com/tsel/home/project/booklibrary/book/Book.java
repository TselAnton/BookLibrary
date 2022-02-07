package com.tsel.home.project.booklibrary.book;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Book implements Serializable {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    private Long id;
    private String name;
    private String author;
    private String publisher;
    private Integer pages;
    private Integer bookshelf;
    private Boolean read;
    private Boolean fullSeries;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public Integer getBookshelf() {
        return bookshelf;
    }

    public void setBookshelf(Integer bookshelf) {
        this.bookshelf = bookshelf;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getFullSeries() {
        return fullSeries;
    }

    public void setFullSeries(Boolean fullSeries) {
        this.fullSeries = fullSeries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id.equals(book.id)
                && Objects.equals(name, book.name)
                && Objects.equals(author, book.author)
                && Objects.equals(publisher, book.publisher)
                && Objects.equals(pages, book.pages)
                && Objects.equals(bookshelf, book.bookshelf)
                && Objects.equals(read, book.read)
                && Objects.equals(fullSeries, book.fullSeries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, author, publisher, pages, bookshelf, read, fullSeries);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", pages=" + pages +
                ", bookshelf=" + bookshelf +
                ", read=" + read +
                ", fullSeries=" + fullSeries +
                '}';
    }
}
