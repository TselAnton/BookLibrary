package com.tsel.home.project.booklibrary.dto;

import com.tsel.home.project.booklibrary.search.SearchField;
import java.util.Objects;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;

public class BookDTO {

    @SearchField(description = "Поиск по названию")
    private String name;

    private ImageView cover;

    @SearchField(description = "Поиск по автору")
    private String author;

    @SearchField(description = "Поиск по издателю")
    private String publisher;

    @SearchField(description = "Поиск по названию цикла")
    private String cycleName;

    private String cycleNumber;

    @SearchField(aliases = {"cycle", "цикл"}, description = "Поиск по законченным циклам")
    private CheckBox cycleEnded;

    @SearchField(aliases = {"read", "прочитано"}, description = "Поиск по прочитанному")
    private CheckBox read;

    @SearchField(aliases = {"sign", "автограф"}, description = "Поиск по наличию автографа")
    private CheckBox autograph;

    @SearchField(aliases = {"pages", "страницы"}, description = "Поиск по количеству страниц")
    private Integer pages;

    @SearchField(aliases = {"price", "цена"}, description = "Поиск по цене")
    private Double price;

    @SearchField(aliases = {"hard", "твердая"}, description = "Поиск по наличию твердой обложки")
    private CheckBox hardCover;

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

    public CheckBox getAutograph() {
        return autograph;
    }

    public void setAutograph(CheckBox autograph) {
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

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public CheckBox getHardCover() {
        return hardCover;
    }

    public void setHardCover(CheckBox hardCover) {
        this.hardCover = hardCover;
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
                && Objects.equals(pages, bookDTO.pages)
                && Objects.equals(price, bookDTO.price)
                && Objects.equals(hardCover, bookDTO.hardCover);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                name,
                author,
                publisher,
                cycleName,
                cycleNumber,
                cycleEnded,
                read,
                autograph,
                pages,
                price,
                hardCover
        );
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
                ", price=" + price +
                ", hardCover=" + hardCover +
                '}';
    }
}
