package com.example.stocksearch;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.icu.util.ICUUncheckedIOException;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class NewsCardsAdapter extends RecyclerView.Adapter {//<NewsCardsAdapter.NewsCardsViewHolder>
    private Context mcontext;
    private ArrayList mNewsCardsList;
    public static final String EXTRA_ID = "newsId";
    private static final String TAG = "hello";
    private String link;

    public interface OnItemClickListener {
        void onItemClick (int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(int position);
    }

    public NewsCardsAdapter(Context context, ArrayList mNewsCardsList) {
        this.mcontext = context;
        this.mNewsCardsList = mNewsCardsList;
    }

    @Override
    public int getItemViewType(int position) {
        if (mNewsCardsList.get(position) instanceof NewsItem)
            return 0;
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mcontext);
//        View v = LayoutInflater.from(mcontext).inflate(R.layout.news_item, parent, false);
        View view;

        view = layoutInflater.inflate(R.layout.news_item, parent,false);
        return new NewsCardsViewHolder(view);


    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (mNewsCardsList.get(position) instanceof NewsItem) {
            //bind news card
            NewsCardsViewHolder newsCardsViewHolder = (NewsCardsViewHolder) holder;
            NewsItem currentItem = (NewsItem) mNewsCardsList.get(position);
            String imageUrl = currentItem.getImageUrl();
            String summary = currentItem.getSummary();
            String time = currentItem.getTime();
            String res = currentItem.getmRes();

            newsCardsViewHolder.mTextViewSummary.setText(summary);
            newsCardsViewHolder.mTextViewTime.setText(getFormattedTime(time));
            newsCardsViewHolder.mTextViewSection.setText(res);
            Picasso.with(mcontext).load(imageUrl).transform(new RoundedCornersTransformation(40, 0)).error(mcontext.getDrawable(R.drawable.no_image)).fit().into(newsCardsViewHolder.mImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mNewsCardsList.size();
    }

    public class NewsCardsViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;
        TextView mTextViewSummary;
        TextView mTextViewTime;
        TextView mTextViewSection;


        public NewsCardsViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.news_image);
            mTextViewSummary = itemView.findViewById(R.id.news_title);
            mTextViewTime = itemView.findViewById(R.id.news_date);
            mTextViewSection = itemView.findViewById(R.id.news_publisher);


            itemView.setOnClickListener(v -> {
                final NewsItem clickedItem = (NewsItem) mNewsCardsList.get(getAdapterPosition());
                link = clickedItem.getmWebUrl();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( clickedItem.getmWebUrl()));
                mcontext.startActivity(intent);
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final NewsItem clickedItem = (NewsItem) mNewsCardsList.get(getAdapterPosition());
                    Dialog dialog = new Dialog(v.getContext());
                    dialog.setContentView(R.layout.dialog);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    //Window window = dialog.getWindow();
                    //window.setLayout(1200, 1200);

                    ImageView imageViewDialog = dialog.findViewById(R.id.dialog_image);
                    TextView textViewDialog = dialog.findViewById(R.id.dialog_title);
                    ImageView imageViewTwitter = dialog.findViewById(R.id.dialog_twitter);
                    ImageView imageViewChrome = dialog.findViewById(R.id.dialog_chrome);

                    Picasso.with(mcontext).load(clickedItem.getImageUrl()).error(mcontext.getDrawable(R.drawable.no_image)).fit().into(imageViewDialog);
                    textViewDialog.setText(clickedItem.getSummary());
                    imageViewTwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=Check out this Link: "
                                    + clickedItem.getmWebUrl() + "&hashtags=CSCI571NEWS"));
                            mcontext.startActivity(intent);
                        }
                    });
                    imageViewChrome.setOnClickListener(v1 -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse( clickedItem.getmWebUrl()));
                        mcontext.startActivity(intent);
                    });
                    dialog.show();
                    return true;
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getFormattedTime(String oTime){
        String str = "";
        String suffix = "ago";

        Date nowTime = new Date();
        Instant instant = Instant.parse(oTime);
        ZoneId z = ZoneId.of( "America/Los_Angeles" );
        ZonedDateTime zdt = instant.atZone(z);
        Date newsTime = Date.from(zdt.toInstant());
        long dateDiff = nowTime.getTime() - newsTime.getTime();
        long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
        long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
        long hour   = TimeUnit.MILLISECONDS.toHours(dateDiff);
        long day  = TimeUnit.MILLISECONDS.toDays(dateDiff);
        if (second < 60) {
            str = second + " seconds " + suffix;
        } else if (minute < 60) {
            str = minute + " minutes " + suffix;
        } else if (hour < 24) {
            str = hour + " hours " + suffix;
        } else {
            if (day == 1) str = day + " day " + suffix;
            else str = day + " days " + suffix;
        }
        return str;
    }
}