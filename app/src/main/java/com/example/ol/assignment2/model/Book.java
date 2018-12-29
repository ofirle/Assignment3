package com.example.ol.assignment2.model;


import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Book implements Serializable {
    private int id;
    private String Title;
    private String Author;
    private String pdf;
    private String Genre;
    private String img;
    private double price;
    private double Rating;
    private int Pages;
    private int Downloads;
    private int year;

    public Book() {
    }

    // C'tor.
    public Book(int i_Id, String i_Author, int i_Downloads, String i_Genre, String i_Img, int i_Pages, double i_Price, double i_Rating, String i_Title, String i_Pdf, int i_Year) {
        this.img = i_Img;
        this.id = i_Id;
        this.Title = i_Title;
        this.Author = i_Author;
        this.Genre = i_Genre;
        this.price = i_Price;
        this.Rating = i_Rating;
        this.Pages = i_Pages;
        this.Downloads = i_Downloads;
        this.year = i_Year;
        this.pdf = i_Pdf;
    }

    // Getters & Setters.
    public String getImg() {
        return img;
    }

    public String getTitle() {
        return Title;
    }

    public String getAuthor() {
        return Author;
    }

    public String getGenre() {
        return Genre;
    }

    public double getPrice() {
        return price;
    }

    public double getRating() {
        return Rating;
    }

    public int getPages() {
        return Pages;
    }

    public int getDownloads() {
        return Downloads;
    }

    public String getPdf() {
        return pdf;
    }

    public void setPdf(String pdf) {
        this.pdf = pdf;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setRating(double rating) {
        Rating = rating;
    }

    public void setPages(int pages) {
        Pages = pages;
    }

    public void setDownloads(int downloads) {
        Downloads = downloads;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Book getBook() {
        return this;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    private Book(Parcel in) {
        this.id = in.readInt();
        this.Title = in.readString();
        this.Author = in.readString();
        this.Genre = in.readString();
        this.img = in.readString();
        this.pdf = in.readString();
        this.price = in.readDouble();
        this.Rating = in.readDouble();
        this.Pages = in.readInt();
        this.Downloads = in.readInt();
    }
}

