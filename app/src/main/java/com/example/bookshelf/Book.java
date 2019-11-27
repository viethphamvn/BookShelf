package com.example.bookshelf;

import java.io.Serializable;

public class Book implements Serializable {
    int id;
    String title;
    String author;
    int published;
    String coverURL;

    public Book(int id, String title, String author, int published, String coverURL){
        this.id = id;
        this.author = author;
        this.title = title;
        this.published = published;
        this.coverURL = coverURL;
    }

    public Book(Book theBook){
        this.id = theBook.id;
        this.author = theBook.author;
        this.title = theBook.title;
        this.published = theBook.published;
        this.coverURL = theBook.coverURL;
    }
}
