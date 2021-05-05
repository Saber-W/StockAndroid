package com.example.stocksearch;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class StatAdapter extends BaseAdapter {

    private final Context context;
    private final String[] stats = new String[7];

    public StatAdapter(Context context, String cur, String low, String bid, String open, String mid, String high, String volume) {
        this.context = context;
        stats[0] = "Current Price: \n" + (cur.equals("null") ? "0.0" : cur);
        stats[1] = "Low: " + (low.equals("null") ? "0.0" : low);
        stats[2] = "Bid Price: " + (bid.equals("null") ? "0.0" : bid);
        stats[3] = "Open Price: \n" + (open.equals("null") ? "0.0" : open);
        stats[4] = "Mid: " + (mid.equals("null") ? "0.0" : mid);
        stats[5] = "High: " + (high.equals("null") ? "0.0" : high);
        stats[6] = "Volume: \n" + (volume.equals("null") ? "0.0" : volume) + ".00";
    }

    @Override
    public int getCount() {
        return stats.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;

        if (convertView == null) {
            textView = new TextView(context);
            textView.setLines(2);
        } else textView = (TextView) convertView;
        textView.setText(stats[position]);
        textView.setTextColor(Color.rgb(0, 0, 0));
        textView.setGravity(Gravity.CENTER);
        textView.setHeight(135);

        return textView;
    }


}
