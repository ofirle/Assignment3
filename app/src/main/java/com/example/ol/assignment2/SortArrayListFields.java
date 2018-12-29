package com.example.ol.assignment2;

import com.example.ol.assignment2.model.Order;
import com.example.ol.assignment2.model.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortArrayListFields {

    public ArrayList<Book> sortPopularity(ArrayList<Book> lst) {
        Collections.sort(lst, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getDownloads() > o2.getDownloads())
                    return -1;
                else if (o1.getDownloads() < o2.getDownloads())
                    return 1;
                return 0;
            }
        });
        return lst;
    }

    public ArrayList<Book> sortRating(ArrayList<Book> lst) {
        Collections.sort(lst, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getRating() > o2.getRating())
                    return -1;
                else if (o1.getRating() < o2.getRating())
                    return 1;
                return 0;
            }
        });
        return lst;
    }

    public ArrayList<Book> sortPriceLowToHigh(ArrayList<Book> lst) {
        Collections.sort(lst, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                if (o1.getPrice() < o2.getPrice())
                    return -1;
                else if (o1.getPrice() > o2.getPrice())
                    return 1;
                return 0;
            }
        });
        return lst;
    }

    public ArrayList<Book> sortBookNameLibrary(ArrayList<Book> lst) {
        Collections.sort(lst, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                return o1.getBook().getTitle().compareTo(o2.getBook().getTitle());
            }
        });
        return lst;
    }

    public ArrayList<Book> sortAuthorNameLibrary(ArrayList<Book> lst) {
        Collections.sort(lst, new Comparator<Book>() {
            @Override
            public int compare(Book o1, Book o2) {
                return o1.getBook().getAuthor().compareTo(o2.getBook().getAuthor());
            }
        });
        return lst;
    }


    public ArrayList<Order> sortBookName(ArrayList<Order> lst) {
        Collections.sort(lst, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.getBook().getTitle().compareTo(o2.getBook().getTitle());
            }
        });
        return lst;
    }

    public ArrayList<Order> sortAuthorName(ArrayList<Order> lst) {
        Collections.sort(lst, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return o1.getBook().getAuthor().compareTo(o2.getBook().getAuthor());
            }
        });
        return lst;
    }

    public ArrayList<Order> sortDate(ArrayList<Order> lst) {
        return lst;
    }

    public ArrayList<Order> sortPrice(ArrayList<Order> lst) {
        Collections.sort(lst, new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                if (o1.getBook().getPrice() < o2.getBook().getPrice())
                    return -1;
                if (o1.getBook().getPrice() > o2.getBook().getPrice())
                    return 1;
                return 0;
            }
        });
        return lst;
    }
}
