package com.example.ol.assignment2.model;

public class Review {
    private String Name;
    private String UserID; // Because GetUID is String
    private String Date;
    private String TextReview;
    private double ScoreReview;

    public Review()
    {

    }

    public Review(String name, String userID, String date, String textReview,double scoreReview) {
        Name = name;
        UserID = userID;
        Date = date;
        TextReview = textReview;
        ScoreReview=scoreReview;
    }



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
