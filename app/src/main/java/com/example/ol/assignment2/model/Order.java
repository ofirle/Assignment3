package com.example.ol.assignment2.model;
import com.example.ol.assignment2.model.Book;

public class Order {
    private Book book;
    private String date;
    private boolean wroteReview;

    public Order(Book book, String date, boolean wroteReview) {
        this.book = book;
        this.date = date;
        this.wroteReview = wroteReview;
    }

    public Book getBook() {
        return book;
    }

    public String getDate() {
        return date;
    }

    public boolean isWroteReview() {
        return wroteReview;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setWroteReview(boolean wroteReview) {
        this.wroteReview = wroteReview;
    }
}


