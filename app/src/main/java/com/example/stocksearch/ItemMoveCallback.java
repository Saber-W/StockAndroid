package com.example.stocksearch;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ItemMoveCallback extends ItemTouchHelper.Callback {

    private final SectionedRecyclerViewAdapter mAdapter;
    private ContactsSection port;
    private ContactsSection fav;
    private final Context context;

    public ItemMoveCallback(Context context, SectionedRecyclerViewAdapter adapter) {
        this.context = context;
        mAdapter = adapter;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }


    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlags, 0);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                          RecyclerView.ViewHolder target) {
        try {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();
            if (inSameSection(fromPosition, toPosition)) {
                List<Contact> list = fromPosition < port.getList().size() ? port.getList() : fav.getList();
                int from = mAdapter.getPositionInSection(fromPosition);
                int to = mAdapter.getPositionInSection(toPosition);
                if (from < to) {
                    for (int i = from; i < to; i++) {
                        Collections.swap(list, i, i + 1);
                    }
                } else {
                    for (int i = from; i > to; i--) {
                        Collections.swap(list, i, i - 1);
                    }
                }
                mAdapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
        return false;
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (viewHolder instanceof ItemViewHolder) {
                ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
                myViewHolder.rootView.setBackgroundColor(Color.GRAY);
            }

        }

        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);

        if (viewHolder instanceof ItemViewHolder) {
            ItemViewHolder myViewHolder = (ItemViewHolder) viewHolder;
            myViewHolder.rootView.setBackgroundColor(ContextCompat.getColor(context, R.color.my_default_color));
        }
    }

    private boolean inSameSection(int from, int to) {
        port = (ContactsSection) mAdapter.getSection("PORTFOLIO");
        fav = (ContactsSection) mAdapter.getSection("FAVORITES");
        if (from <= port.getList().size()) {
            if (to > port.getList().size()) return false;
            if (to == 0) return false;
        } else {
            if (to <= port.getList().size()) return false;
        }
        return true;
    }

}