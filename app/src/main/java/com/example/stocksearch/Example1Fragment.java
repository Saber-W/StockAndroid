package com.example.stocksearch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;

import java.math.BigDecimal;
import java.util.*;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class Example1Fragment extends Fragment implements ContactsSection.ClickListener {

    private static final String DIALOG_TAG = "SectionItemInfoDialogTag";

    private SectionedRecyclerViewAdapter sectionedAdapter;
    private RecyclerView recyclerView;
    private SharedPreferences sp;
    private ContactsSection port;
    private ContactsSection fav;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        sp = requireContext().getSharedPreferences("stock_data", Context.MODE_PRIVATE);
        final View view = inflater.inflate(R.layout.fragment_ex1, container, false);
        sectionedAdapter = new SectionedRecyclerViewAdapter();
        recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Set<String> portTickers = sp.getStringSet("portfolio", new LinkedHashSet<>());
        Set<String> favTickers = sp.getStringSet("favorites", new LinkedHashSet<>());
        LinkedHashSet<String> allTickers = new LinkedHashSet<>();
        allTickers.addAll(portTickers);
        allTickers.addAll(favTickers);
        if (allTickers.size() > 0) {
            String ticker = "";
            for (String s : allTickers) {
                ticker += s.split(",")[0] + ",";
            }
            ticker = ticker.substring(0, ticker.length() - 1);
            String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/price/" + ticker;
            JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null,
                            response -> {
                                Map<String, List<String>> priceInfo = new HashMap<>();
                                double totalValue = 0;
                                Set<String> seen = new HashSet<>();
                                for (int i = 0; i < response.length(); i++) {
                                    String last = "", change = "", curTicker = "";
                                    try {
                                        last = response.getJSONObject(i).getString("last");
                                        change = new BigDecimal(last).subtract(new BigDecimal(response.getJSONObject(i).getString("prevClose"))).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                        curTicker = response.getJSONObject(i).getString("ticker");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    List<String> list = priceInfo.getOrDefault(curTicker, new ArrayList<>());
                                    list.add(last);
                                    list.add(change);
                                    priceInfo.put(curTicker, list);
                                }
                                List<Contact> portContact = new ArrayList<>();
                                List<Contact> favContact = new ArrayList<>();
                                for (String curTicker : portTickers) {
                                    String[] tickerAndName = curTicker.split(",");
                                    String share = sp.getString(tickerAndName[0], "0");
                                    boolean hasShare = !share.equals("0");
                                    List<String> priceAndChange = priceInfo.get(tickerAndName[0]);
                                    if (hasShare) {
                                        totalValue += new BigDecimal(share).multiply(new BigDecimal(priceAndChange.get(0))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                        seen.add(tickerAndName[0]);
                                    }
                                    int image = Double.parseDouble(priceAndChange.get(1)) > 0 ? R.drawable.ic_twotone_trending_up_24 : R.drawable.ic_baseline_trending_down_24;
                                    Contact contact = new Contact(tickerAndName[0], tickerAndName[1], share, priceAndChange.get(0), priceAndChange.get(1), hasShare, image);
                                    portContact.add(contact);
                                }
                                for (String curTicker : favTickers) {
                                    String[] tickerAndName = curTicker.split(",");
                                    String share = sp.getString(tickerAndName[0], "0");
                                    boolean hasShare = !share.equals("0");
                                    List<String> priceAndChange = priceInfo.get(tickerAndName[0]);
                                    if (hasShare && !seen.contains(tickerAndName[0])) {
                                        totalValue += new BigDecimal(share).multiply(new BigDecimal(priceAndChange.get(0))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                    }
                                    int image = Double.parseDouble(priceAndChange.get(1)) > 0 ? R.drawable.ic_twotone_trending_up_24 : R.drawable.ic_baseline_trending_down_24;
                                    Contact contact = new Contact(tickerAndName[0], tickerAndName[1], share, priceAndChange.get(0), priceAndChange.get(1), hasShare, image);
                                    favContact.add(contact);
                                }
                                String remain = sp.getString("networth", "0");
                                Log.d("Remain Money", "amount of money left is " + remain);
                                String netValue = new BigDecimal(remain).add(BigDecimal.valueOf(totalValue)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                                sectionedAdapter.addSection("PORTFOLIO", (port = new ContactsSection("PORTFOLIO", portContact, this, netValue, true)));
                                sectionedAdapter.addSection("FAVORITES", (fav = new ContactsSection("FAVORITES", favContact, this, netValue, false)));
                                recyclerView.setAdapter(sectionedAdapter);
                                MainActivity.stopSpinner();
                            }, error -> Log.e("VOLLEY", "getPrice method went wrong"));
            MySingleton.getInstance(requireContext().getApplicationContext()).addToRequestQueue(jsonArrayRequest);
            ItemTouchHelper ith = new ItemTouchHelper(new ItemMoveCallback(requireContext(), sectionedAdapter));
            ith.attachToRecyclerView(recyclerView);
            enableSwipeToDelete();
        }

        return view;
    }

    @Override
    public void onItemRootViewClicked(@NonNull final ContactsSection section, final int itemAdapterPosition) {
        Intent intent = new Intent(requireContext(), DisplayMessageActivity.class);
        intent.putExtra("com.example.stocksearch.MESSAGE", section.getList().get(sectionedAdapter.getPositionInSection(itemAdapterPosition)).ticker); // key and value
        startActivity(intent);
    }

    public void enableSwipeToDelete() {
        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(requireContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                final int position = viewHolder.getAdapterPosition();
                if (position > port.getList().size()) {
                    final String ticker = fav.getList().get(sectionedAdapter.getPositionInSection(position)).ticker;
                    final String companyName = fav.getList().get(sectionedAdapter.getPositionInSection(position)).name;

                    sectionedAdapter.notifyItemRemoved(position);
                    fav.getList().remove(sectionedAdapter.getPositionInSection(position));
                    Set<String> set = new LinkedHashSet<>(sp.getStringSet("favorites", new LinkedHashSet<>()));
                    set.remove(ticker + "," + companyName);
                    sp.edit().putStringSet("favorites", set).apply();
                }
            }
        };

        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchhelper.attachToRecyclerView(recyclerView);
    }
}
