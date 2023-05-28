package com.tsel.home.project.booklibrary.dto;

import java.util.Objects;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;

public class BookDTO {

    private String name;
    private ImageView cover;
    private String author;
    private String publisher;
    private String cycleName;
    private String cycleNumber;
    private CheckBox cycleEnded;
    private CheckBox read;
    private boolean autograph;
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

    public boolean isAutograph() {
        return autograph;
    }

    public void setAutograph(boolean autograph) {
        this.autograph = autograph;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public ImageView getCover() {
        return cover;
    }

    public void setCover(ImageView cover) {
        this.cover = cover;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookDTO bookDTO = (BookDTO) o;
        return Objects.equals(name, bookDTO.name)
                && Objects.equals(author, bookDTO.author)
                && Objects.equals(publisher, bookDTO.publisher)
                && Objects.equals(cycleName, bookDTO.cycleName)
                && Objects.equals(cycleNumber, bookDTO.cycleNumber)
                && Objects.equals(cycleEnded, bookDTO.cycleEnded)
                && Objects.equals(read, bookDTO.read)
                && Objects.equals(autograph, bookDTO.autograph)
                && Objects.equals(pages, bookDTO.pages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, author, publisher, cycleName, cycleNumber, cycleEnded, read, autograph, pages);
    }

    @Override
    public String toString() {
        return "BookDTO{" +
                "name='" + name + '\'' +
                ", author='" + author + '\'' +
                ", publisher='" + publisher + '\'' +
                ", cycleName='" + cycleName + '\'' +
                ", cycleNumber='" + cycleNumber + '\'' +
                ", cycleEnded=" + cycleEnded +
                ", read=" + read +
                ", autograph=" + autograph +
                ", pages=" + pages +
                '}';
    }
}
