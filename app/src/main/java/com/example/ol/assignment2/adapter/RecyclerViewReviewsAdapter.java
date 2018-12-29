package com.example.ol.assignment2.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.ol.assignment2.R;
import com.example.ol.assignment2.model.Review;

import java.util.List;

public class RecyclerViewReviewsAdapter extends RecyclerView.Adapter<RecyclerViewReviewsAdapter.MyViewHolder> {
    private Context mContext;
    private List<Review> mData;
    private Typeface myFont;

    public RecyclerViewReviewsAdapter(Context i_Context, List<Review> i_Data, Typeface i_MyFont) {
        this.mContext = i_Context;
        this.mData = i_Data;
        myFont = i_MyFont;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        view = mInflater.inflate(R.layout.item_review, viewGroup, false);
        return new RecyclerViewReviewsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        myViewHolder.txtNameReview.setText(mData.get(i).getName());
        myViewHolder.txtDateReview.setText(mData.get(i).getDate());
        myViewHolder.txtTextReview.setText(mData.get(i).getTextReview());
        myViewHolder.txtNameReview.setTypeface(myFont);
        myViewHolder.txtDateReview.setTypeface(myFont);
        myViewHolder.txtTextReview.setTypeface(myFont);
        myViewHolder.ratingBarReview.setRating((float) (mData.get(i).getScoreReview()));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView txtNameReview, txtDateReview, txtTextReview;
        RatingBar ratingBarReview;
        CardView cardView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNameReview = (TextView) itemView.findViewById(R.id.txtNameReview);
            txtDateReview = (TextView) itemView.findViewById(R.id.txtDateReview);
            txtTextReview = (TextView) itemView.findViewById(R.id.txtTextReview);
            ratingBarReview = (RatingBar) itemView.findViewById(R.id.ratingBarReview);
            cardView = (CardView) itemView.findViewById(R.id.cardviewIDOrder);
        }
    }
}