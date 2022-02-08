package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Objects;

public class Book implements BaseEntity {

    @Serial
    private static final long serialVersionUID = -88421120419730807L;

    private String name;
    private String author;
    private String publisher;
    private Integer pages;
    private Integer bookshelf;
    private Boolean read;
    private String cycleName;
    private Integer numberInSeries;
    private String coverImgAbsolutePath;

    @Override
    public String getKey() {
        String compositeKey = name + "_" + author;
        if (numberInSeries != null) {
            compositeKey += "_" + numberInSeries;
        }
        return compositeKey;
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

    public String getCycleName() {
        return cycleName;
    }

    public void setCycleName(String cycleName) {
        this.cycleName = cycleName;
    }

    public Integer getNumberInSeries() {
        return numberInSeries;
    }

    public void setNumberInSeries(Integer numberInSeries) {
        this.numberInSeries = numberInSeries;
    }

    public String getCoverImgAbsolutePath() {
        return coverImgAbsolutePath;
    }

    public void setCoverImgAbsolutePath(String coverImgAbsolutePath) {
        this.coverImgAbsolutePath = coverImgAbsolutePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return name.equals(book.name)
                && author.equals(book.author)
                && publisher.equals(book.publisher)
                && pages.equals(book.pages)
                && Objects.equals(bookshelf, book.bookshelf)
                && Objects.equals(read, book.read)
                && Objects.equals(cycleName, book.cycleName)
                && Objects.equals(numberInSeries, book.numberInSeries)
                && Objects.equals(coverImgAbsolutePath, book.coverImgAbsolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author, publisher, pages, bookshelf, read, cycleName, numberInSeries, coverImgAbsolutePath);
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", pages=" + pages +
                ", bookshelf=" + bookshelf +
                ", read=" + read +
                ", cycleName='" + cycleName + '\'' +
                ", numberInSeries=" + numberInSeries +
                ", coverImgAbsolutePath=" + coverImgAbsolutePath +
                '}';
    }
}
