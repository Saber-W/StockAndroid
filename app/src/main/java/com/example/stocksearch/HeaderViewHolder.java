package com.example.stocksearch;

import android.util.TypedValue;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class HeaderViewHolder extends RecyclerView.ViewHolder {

    final TextView tvTitle;
    final TextView networth;
    final TextView remain;

    HeaderViewHolder(@NonNull View view) {
        super(view);

        tvTitle = view.findViewById(R.id.tvTitle);
        networth = view.findViewById(R.id.networth);
        remain = view.findViewById(R.id.remain_networth);
    }

    public void setVisibility(boolean isPort) {
        if (!isPort) {
            networth.setVisibility(View.GONE);
            remain.setVisibility(View.GONE);
//            networth.setHeight(0);
//            networth.setWidth(0);
//            remain.setHeight(0);
//            remain.setWidth(0);
            RecyclerView.LayoutParams param = (RecyclerView.LayoutParams) itemView.getLayoutParams();
            param.height = 136;
        }
    }
}
