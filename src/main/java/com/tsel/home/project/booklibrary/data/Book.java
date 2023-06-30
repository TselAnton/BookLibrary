package com.tsel.home.project.booklibrary.data;

import java.io.Serial;
import java.util.Locale;
import java.util.Objects;

public class Book implements BaseEntity {

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
    private Double price;

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

    @Deprecated
    public Integer getBookshelf() {
        return bookshelf;
    }

    @Deprecated
    public void setBookshelf(Integer bookshelf) {
        this.bookshelf = bookshelf;
    }

    public Boolean getRead() {
        return read;
    }

    public void setRead(Boolean read) {
        this.read = read;
    }

    public Boolean getAutograph() {
        return autograph;
    }

    public void setAutograph(Boolean autograph) {
        this.autograph = autograph;
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
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
                && Objects.equals(read, book.read)
                && Objects.equals(autograph, book.autograph)
                && Objects.equals(cycleName, book.cycleName)
                && Objects.equals(numberInSeries, book.numberInSeries)
                && Objects.equals(coverImgAbsolutePath, book.coverImgAbsolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author, publisher, pages, read, cycleName, numberInSeries, coverImgAbsolutePath);
    }

    @Override
    public String toString() {
        return "Book{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", pages=" + pages +
                ", read=" + read +
                ", autograph=" + autograph +
                ", cycleName='" + cycleName + '\'' +
                ", numberInSeries=" + numberInSeries +
                ", coverImgAbsolutePath=" + coverImgAbsolutePath +
                '}';
    }
}
