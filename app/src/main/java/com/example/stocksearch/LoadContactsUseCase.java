package com.example.stocksearch;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class LoadContactsUseCase {

    private SharedPreferences sp;
    private Map<String, List<String>> data;
    private Context context;
    private int requestCnt = 0;

    Map<String, List<Contact>> execute(@NonNull final Context context) {
        this.context = context;
        sp = context.getSharedPreferences("stock_data", Context.MODE_PRIVATE);
        Set<String> portfolio = sp.getStringSet("portfolio", new LinkedHashSet<>());
        Set<String> favorites = sp.getStringSet("favorites", new LinkedHashSet<>());
        sp.edit().putStringSet("favorites", new LinkedHashSet<>()).apply();
//        Log.e("1712:2", favorites.size() + "");
//        Set<String> tickers = new LinkedHashSet<>();
//        Map<String, String> shares = new HashMap<>();
        Map<String, List<Contact>> map = new LinkedHashMap<>();
//
//        tickers.addAll(portfolio);
//        tickers.addAll(favorites);
//        for (String ticker : portfolio) shares.put(ticker, sp.getString(ticker, "0.0"));
//        data = getPrice(tickers);
        //map.put("PORTFOLIO", getContacts(portfolio));
        //map.put("FAVORITES", getContacts(favorites));

        return map;
    }

    private List<Contact> getContacts(Set<String> tickers) {
        final List<Contact> contactsList = new ArrayList<>();
        Log.e("1712", contactsList.size() + "");

        for (final String ticker : tickers) {
            List<String> tickerData = data.get(ticker);
            boolean hasShare = false;
            int image;
            if (tickerData.get(2) != null) hasShare = true;
            if (Double.parseDouble(tickerData.get(4)) > 0) image = R.drawable.ic_twotone_trending_up_24;
            else image = R.drawable.ic_baseline_trending_down_24;
            contactsList.add(new Contact(tickerData.get(0), tickerData.get(1), tickerData.get(2), tickerData.get(3), tickerData.get(4), hasShare, image));
        }

        return contactsList;
    }

    private Map<String, List<String>> getPrice(Set<String> tickers) {
        Map<String, List<String>> ans = new HashMap<>();

        String ticker = "";
        for (String s : tickers) ticker += s;
        String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/price/" + ticker;
        Log.e("1811", url);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        response -> {
                            try {
                                Log.e("1809", response.length() + "");
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject jsonObject = response.getJSONObject(i);
                                    List<String> list = new ArrayList<>();
                                    String detailUrl = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/details/" + jsonObject.getString("ticker");
                                    list.add(jsonObject.getString("ticker"));
                                    list.add(sp.getString(jsonObject.getString("ticker"), "0"));
                                    list.add(jsonObject.getString("last"));
                                    list.add(new BigDecimal(jsonObject.getString("last")).subtract(new BigDecimal(jsonObject.getString("prevClose"))).toString());
//                                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
//                                            (Request.Method.GET, detailUrl, null, response1 -> {
//                                                try {
//                                                    list.add(1, response1.getString("name"));
//                                                    requestCnt--;
//                                                } catch (JSONException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }, error -> {});
//                                    MySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
//                                    requestCnt++;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            //requestCnt--;
                        }, error -> Log.e("VOLLEY", "getPrice went wrong"));
        MySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
        requestCnt++;

        //while (requestCnt > 0) {
            //Log.e("1808", requestCnt + "");
        //}

        return ans;
    }

}
