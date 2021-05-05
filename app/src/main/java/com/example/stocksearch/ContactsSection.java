package com.example.stocksearch;

import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

// equals to the Adapter
final class ContactsSection extends Section {

    private final String title;
    private final String remain;
    private final List<Contact> list;
    private final ClickListener clickListener;
    private boolean isPort;

    ContactsSection(@NonNull final String title, @NonNull final List<Contact> list,
                    @NonNull final ClickListener clickListener, @NonNull final String remain, boolean isPort) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.section_ex1_item)
                .headerResourceId(R.layout.networth)
                .build());

        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
        this.remain = remain;
        this.isPort = isPort;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ItemViewHolder itemHolder = (ItemViewHolder) holder;

        final Contact contact = list.get(position);

        itemHolder.ticker.setText(contact.ticker);
        if (contact.hasShare) itemHolder.shareOrName.setText(contact.share + " shares");
        else itemHolder.shareOrName.setText(contact.name);
        itemHolder.price.setText(contact.price);
        itemHolder.change.setText(Math.abs(Double.parseDouble(contact.change)) + "");
        itemHolder.upOrDown.setImageResource(contact.image);
        if (Double.parseDouble(contact.change) > 0) itemHolder.change.setTextColor(Color.rgb(49, 156, 94));
        else itemHolder.change.setTextColor(Color.rgb(155, 64, 73));

        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(this, itemHolder.getAdapterPosition())
        );
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.tvTitle.setText(title);
        headerHolder.remain.setText(remain);
        headerHolder.setVisibility(isPort);
    }

    public List<Contact> getList() {
        return list;
    }

    interface ClickListener {
        void onItemRootViewClicked(@NonNull final ContactsSection section, final int itemAdapterPosition);
    }
}
