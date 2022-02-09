package com.tsel.home.project.booklibrary.dto;

import java.util.Objects;

public class BookDTO {

    private String name;
    private String author;
    private String shelf;
    private String cycleName;
    private String cycleNumber;
    private String read;

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

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDTO bookDTO = (BookDTO) o;
        return Objects.equals(name, bookDTO.name)
                && Objects.equals(author, bookDTO.author)
                && Objects.equals(shelf, bookDTO.shelf)
                && Objects.equals(cycleName, bookDTO.cycleName)
                && Objects.equals(cycleNumber, bookDTO.cycleNumber)
                && Objects.equals(read, bookDTO.read);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author, shelf, cycleName, cycleNumber, read);
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", shelf='" + shelf + '\'' +
                ", cycleName='" + cycleName + '\'' +
                ", cycleNumber='" + cycleNumber + '\'' +
                ", read='" + read + '\'' +
                '}';
    }
}
