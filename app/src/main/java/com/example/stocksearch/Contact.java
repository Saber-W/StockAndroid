package com.example.stocksearch;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

final class Contact {
    final String ticker;
    final String name;
    final String share;
    final String price;
    final String change;
    final boolean hasShare;
    @DrawableRes final int image;

    Contact(@NonNull final String ticker, @NonNull final String name, @NonNull final String share,
            @NonNull final String price, @NonNull final String change, final boolean hasShare,
            @DrawableRes final int image) {
        this.ticker = ticker;
        this.name = name;
        this.share = share;
        this.price = price;
        this.change = change;
        this.hasShare = hasShare;
        this.image = image;
    }
}
