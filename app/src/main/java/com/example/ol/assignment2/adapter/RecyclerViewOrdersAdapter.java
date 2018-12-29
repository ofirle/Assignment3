package com.example.ol.assignment2.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import com.example.ol.assignment2.model.Order;
import com.example.ol.assignment2.model.User;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.List;

public class RecyclerViewOrdersAdapter extends RecyclerView.Adapter<RecyclerViewOrdersAdapter.MyViewHolder> {
    private Context mContext;
    private List<Order> mData;
    private User mUser;
    private List<Book> mAllBooks;
    private String mActivityFrom;
    private Typeface myFont;


    public RecyclerViewOrdersAdapter(Context i_Context, List<Order> i_Data, User i_User, String i_ActivityFrom, Typeface i_MyFont, List<Book> i_AllBooks) {
        this.mContext = i_Context;
        this.mData = i_Data;
        this.mUser = i_User;
        this.mActivityFrom = i_ActivityFrom;
        this.myFont = i_MyFont;
        this.mAllBooks = i_AllBooks;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view;
        LayoutInflater mInflater = LayoutInflater.from(mContext);

        view = mInflater.inflate(R.layout.item_orderd_book, viewGroup, false);
        return new RecyclerViewOrdersAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, final int i) {
        myViewHolder.iv_BookCover.setScaleType(ImageView.ScaleType.FIT_XY);
        Picasso.with(mContext).load(mData.get(i).getBook().getImg()).into(myViewHolder.iv_BookCover);
        myViewHolder.tvTitle.setText(mData.get(i).getBook().getTitle());
        myViewHolder.tvAuthor.setText(mData.get(i).getBook().getAuthor());
        myViewHolder.tvPrice.setText(Double.toString(mData.get(i).getBook().getPrice()) + "$");
        myViewHolder.tvDate.setText(mData.get(i).getDate());
        myViewHolder.tvTitle.setTypeface(myFont);
        myViewHolder.tvAuthor.setTypeface(myFont);
        myViewHolder.tvPrice.setTypeface(myFont);
        myViewHolder.tvDate.setTypeface(myFont);

        myViewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, BookActivity.class);
                Book theBook = mData.get(i).getBook();
                intent.putExtra("choseBook", theBook);
                intent.putExtra("activityFrom", mActivityFrom);
                intent.putExtra("user", mUser);
                intent.putExtra("booksList", (Serializable) mAllBooks);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_BookCover;
        TextView tvTitle, tvAuthor, tvPrice, tvDate;
        CardView cardView;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            iv_BookCover = (ImageView) itemView.findViewById(R.id.ivBookCover);
            tvTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            tvAuthor = (TextView) itemView.findViewById(R.id.txtAuthor);
            tvPrice = (TextView) itemView.findViewById(R.id.txtPrice);
            tvDate = (TextView) itemView.findViewById(R.id.txtDate);
            cardView = (CardView) itemView.findViewById(R.id.cardviewIDOrder);
        }
    }
}