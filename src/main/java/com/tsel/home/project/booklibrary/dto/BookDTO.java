package com.tsel.home.project.booklibrary.dto;

import javafx.scene.control.CheckBox;

import java.util.Objects;

public class BookDTO {

    private String name;
    private String author;
    private String publisher;
    private String shelf;
    private String cycleName;
    private String cycleNumber;
    private CheckBox cycleEnded;
    private CheckBox read;
    private Integer pages;

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

    public String getShelf() {
        return shelf;
    }

    public void setShelf(String shelf) {
        this.shelf = shelf;
    }

    public String getCycleName() {
        return cycleName;
    }

    public void setCycleName(String cycleName) {
        this.cycleName = cycleName;
    }

    public String getCycleNumber() {
        return cycleNumber;
    }

    public void setCycleNumber(String cycleNumber) {
        this.cycleNumber = cycleNumber;
    }

    public CheckBox getCycleEnded() {
        return cycleEnded;
    }

    public void setCycleEnded(CheckBox cycleEnded) {
        this.cycleEnded = cycleEnded;
    }

    public CheckBox getRead() {
        return read;
    }

    public void setRead(CheckBox read) {
        this.read = read;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDTO bookDTO = (BookDTO) o;
        return Objects.equals(name, bookDTO.name)
                && Objects.equals(author, bookDTO.author)
                && Objects.equals(shelf, bookDTO.shelf)
                && Objects.equals(publisher, bookDTO.publisher)
                && Objects.equals(cycleName, bookDTO.cycleName)
                && Objects.equals(cycleNumber, bookDTO.cycleNumber)
                && Objects.equals(cycleEnded, bookDTO.cycleEnded)
                && Objects.equals(read, bookDTO.read)
                && Objects.equals(pages, bookDTO.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author, publisher, shelf, cycleName, cycleNumber, cycleEnded, read, pages);
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", shelf='" + shelf + '\'' +
                ", cycleName='" + cycleName + '\'' +
                ", cycleNumber='" + cycleNumber + '\'' +
                ", cycleEnded=" + cycleEnded +
                ", read=" + read +
                ", pages=" + pages +
                '}';
    }
}
