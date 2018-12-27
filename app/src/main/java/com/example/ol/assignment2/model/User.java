package com.example.ol.assignment2.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    private String email;
    private int totalPurchase;
    private List<Integer> myBooksID;

    public User() {

    }

    public User(String email, int totalPurchase, List<Integer> myBooksID) {
        this.email = email;
        this.totalPurchase = totalPurchase;
        this.myBooksID = myBooksID;
    }

    public List<Integer> getMyBooks() {
        return this.myBooksID;
    }

    public int getTotalPurchase() {
        return totalPurchase;
    }


    public void setTotalPurchase(int totalPurchase) {
        this.totalPurchase = totalPurchase;
    }

    public void setMyBooks(List<Integer> myBooksID) {
        this.myBooksID = myBooksID;
    }

    public String getEmail() {
        return email;
    }


    public void upgdateTotalPurchase(int newPurcahsePrice) {
        this.totalPurchase += newPurcahsePrice;
    }


    public User(Parcel in) {
        this.email = in.readString();
        this.totalPurchase = in.readInt();
        in.readList(myBooksID, String.class.getClassLoader());
    }


}
