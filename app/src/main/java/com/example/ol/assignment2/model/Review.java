package com.example.ol.assignment2.model;


public class Review {
    private String Name;
    private String UserID; // Because GetUID is String
    private String Date;
    private String TextReview;
    private double ScoreReview;

    public Review() {

    }


    // C'tor.
    public Review(String i_Name, String i_UserID, String i_Date, String i_TextReview, double i_ScoreReview) {
        Name = i_Name;
        UserID = i_UserID;
        Date = i_Date;
        TextReview = i_TextReview;
        ScoreReview = i_ScoreReview;
    }


    // Getters & Setters.
    public double getScoreReview() {

        return ScoreReview;
    }

    public String getName() {
        return Name;
    }

    public String getUserID() {
        return UserID;
    }

    public String getDate() {
        return Date;
    }

    public String getTextReview() {
        return TextReview;
    }

    public void setName(String name) {
        Name = name;
    }

    public void setUserID(String userID) {
        UserID = userID;
    }

    public void setDate(String date) {
        Date = date;
    }

    public void setTextReview(String textReview) {
        TextReview = textReview;
    }

    public void setScoreReview(double scoreReview) {
        ScoreReview = scoreReview;
    }
}
