package com.tsel.home.project.booklibrary;

import com.tsel.home.project.booklibrary.repository.impl.BookRepository;

public class AppScript {

    public static void main(String[] args) {
        BookRepository oldBookRepository = new BookRepository("G:/MyProjects/IdeaProjects/BookLibrary/script/old/my-library-books-storage.txt");
        BookRepository newBookRepository = new BookRepository("G:/MyProjects/IdeaProjects/BookLibrary/script/new/my-library-books-storage.txt");

        System.out.println("Start script");
    }
}
