package com.example.ol.assignment2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ol.assignment2.BookActivity;
import com.example.ol.assignment2.R;
import com.example.ol.assignment2.model.Book;
import com.example.ol.assignment2.model.User;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private Context mContext;
    private List<Book> mData;
    private List<Book> mAllBooks;
    private User mUser;
    private String mActivityFrom;
    private Typeface myFont;


    public RecyclerViewAdapter(Context i_Context, List<Book> i_Data, User i_User, String i_Activityfrom, Typeface i_MyFont, List<Book> i_AllBooks) {
        this.mContext = i_Context;
        this.mData = i_Data;
        this.mUser = i_User;
        this.mActivityFrom = i_Activityfrom;
        this.myFont = i_MyFont;
        this.mAllBooks = i_AllBooks;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);

        view = mInflater.inflate(R.layout.item_book, viewGroup, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        Uri myUri = Uri.parse(mData.get(i).getImg());
        Picasso.with(mContext).load(myUri).into(myViewHolder.iv_BookCover);
        myViewHolder.iv_BookCover.setScaleType(ImageView.ScaleType.FIT_XY);
        myViewHolder.tvPrice.setText(Double.toString(mData.get(i).getPrice()));
        myViewHolder.tvRating.setText(Double.toString(mData.get(i).getRating()));
        myViewHolder.tvPrice.setTypeface(myFont);
        myViewHolder.tvRating.setTypeface(myFont);

        myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BookActivity.class);
                Book theBook = mData.get(i).getBook();
                intent.putExtra("choseBook", theBook);
                intent.putExtra("user", mUser);
                intent.putExtra("activityFrom", mActivityFrom);
                intent.putExtra("booksList", (Serializable) mAllBooks);
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void filterList(ArrayList<Book> filteredList) {
        mData = filteredList;
        notifyDataSetChanged();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_BookCover;
        TextView tvPrice, tvRating;
        CardView cardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_BookCover = (ImageView) itemView.findViewById(R.id.ivBookCover);
            tvPrice = itemView.findViewById(R.id.txtPrice);
            tvRating = itemView.findViewById(R.id.txtRating);
            cardView = (CardView) itemView.findViewById(R.id.cardviewIDBook);
        }
    }
}
