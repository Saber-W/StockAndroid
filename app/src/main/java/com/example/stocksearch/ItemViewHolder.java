package com.example.stocksearch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class ItemViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final TextView ticker;
    final TextView shareOrName;
    final TextView price;
    final TextView change;
    final ImageView upOrDown;
    final View line;

    ItemViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        ticker = view.findViewById(R.id.ticker);
        shareOrName = view.findViewById(R.id.shares_or_name);
        price = view.findViewById(R.id.price);
        change = view.findViewById(R.id.change);
        upOrDown = view.findViewById(R.id.up_or_down);
        line = view.findViewById(R.id.section_line);
    }
}
