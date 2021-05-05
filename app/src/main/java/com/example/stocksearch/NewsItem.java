package com.example.stocksearch;

import java.util.Objects;

public class NewsItem {
    private String mImageUrl;
    private String mSummary;
    private String mTime;
    private String mWebUrl;
    private String mres;

    public NewsItem(String imageUrl, String summary, String time, String webUrl, String res) {
        mImageUrl = imageUrl;
        mSummary = summary;
        mTime = time;
        mWebUrl = webUrl;
        mres = res;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public String getSummary() {
        return mSummary;
    }

    public String getTime() {
        return mTime;
    }

    public String getmWebUrl() {
        return mWebUrl;
    }

    public String getmRes() { return mres;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsItem newsItem = (NewsItem) o;
        return Objects.equals(mSummary, newsItem.mSummary);
//        &&
//                Objects.equals(mTime, newsItem.mTime);
    }

    @Override
    public int hashCode() {
//        return Objects.hash(mSummary, mTime);
        return Objects.hash(mSummary);
    }
}