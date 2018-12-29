package com.example.ol.assignment2;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

public class SortAdapter extends ArrayAdapter<SortItem> {
    public SortAdapter(Context context, ArrayList<SortItem> sortList) {
        super(context, 0, sortList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_sort, parent, false
            );
        }

        ImageView imageView = convertView.findViewById(R.id.sort_icon);
        SortItem currentItem = getItem(position);
        if (currentItem != null) {
            imageView.setImageResource(currentItem.getIconSort());
        }
        return convertView;
    }
}
