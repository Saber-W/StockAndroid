package com.example.stocksearch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DisplayMessageActivity extends AppCompatActivity {

    private String ticker;
    private GridView statsView;
    private String chartData = "";
    private WebView highchart;
    private RecyclerView newsView;
    private String companyName;
    private String curPrice;
    private SharedPreferences sp;
    private ProgressBar spinner;
    private TextView spinnerText;
    private NestedScrollView detailPage;
    private CardView firstNews;
    private String firstURL;
    private String firstImageURL;
    private String firstTitle;
    private boolean flag1 = false, flag2 = false, flag3 = false;
    private Context context;
    private JSONArray price;
    private JSONObject news;
    private JSONArray hist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        sp = this.getSharedPreferences("stock_data", Context.MODE_PRIVATE);

        MainActivity.stopHandler();
        ticker = getIntent().getStringExtra(MainActivity.EXTRA_MESSAGE).toUpperCase();
        (spinner = findViewById(R.id.detail_progressBar)).setVisibility(View.VISIBLE);
        spinnerText = findViewById(R.id.detail_progressBar_text);
        (detailPage = findViewById(R.id.detail_page)).setVisibility(View.GONE);

        highchart = findViewById(R.id.detail_chart);
        WebSettings webSettings = highchart.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        getDetail(ticker);
        getPrice(ticker);
        getHist(ticker);
        getNews(ticker);

        Toolbar myToolbar = findViewById(R.id.my_detail_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        TextView textView = findViewById(R.id.detail_ticker);
        textView.setText(ticker);

        statsView = findViewById(R.id.detail_stats_view);

        // if line is less than two lines, do not show button
        Button showLess = findViewById(R.id.detail_show_less);
        showLess.setVisibility(View.GONE);

        highchart.setBackgroundColor(Color.TRANSPARENT);
        firstNews = findViewById(R.id.detail_news_first);
        firstNews.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstURL));
            this.startActivity(intent);
        });
        firstNews.setOnLongClickListener(v -> {
            Dialog dialog = new Dialog(v.getContext());
            dialog.setContentView(R.layout.dialog);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //Window window = dialog.getWindow();
            //window.setLayout(1200, 1200);

            ImageView imageViewDialog = dialog.findViewById(R.id.dialog_image);
            TextView textViewDialog = dialog.findViewById(R.id.dialog_title);
            ImageView imageViewTwitter = dialog.findViewById(R.id.dialog_twitter);
            ImageView imageViewChrome = dialog.findViewById(R.id.dialog_chrome);

            Picasso.with(this).load(firstImageURL).error(getDrawable(R.drawable.no_image)).fit().into(imageViewDialog);
            textViewDialog.setText(firstTitle);
            imageViewTwitter.setOnClickListener(v12 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet?text=Check out this Link: "
                        + firstURL + "&hashtags=CSCI571NEWS"));
                this.startActivity(intent);
            });
            imageViewChrome.setOnClickListener(v1 -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstURL));
                this.startActivity(intent);
            });
            dialog.show();
            return true;
        });
        this.context = this;

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopSpinner();
                if (!(flag1 && flag2 && flag3)) handler.postDelayed(this, 2000);
            }
        }, 2000);
    }

    private void getPrice(String ticker) {
        String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/price/" + ticker;
        final TextView curPriceView = findViewById(R.id.detail_cur_price);
        final TextView changeView = findViewById(R.id.detail_change);
        TextView ownInfo = findViewById(R.id.detail_own_info);
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        response -> {
                            try {
                                JSONObject jsonObject = response.getJSONObject(0);
                                curPriceView.setText("$" + jsonObject.getString("last"));
                                double change = new BigDecimal(jsonObject.getString("last")).subtract(new BigDecimal(jsonObject.getString("prevClose"))).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                                if (change > 0) {
                                    changeView.setText("$" + change);
                                    changeView.setTextColor(Color.rgb(49, 156, 94));
                                }
                                else {
                                    changeView.setText("-$" + Math.abs(change));
                                    changeView.setTextColor(Color.rgb(155, 64, 73));
                                }
                                statsView.setAdapter(new StatAdapter(this,
                                        jsonObject.getString("last"), jsonObject.getString("low"),
                                        jsonObject.getString("bidPrice"), jsonObject.getString("open"),
                                        jsonObject.getString("mid"), jsonObject.getString("high"),
                                        jsonObject.getString("volume")));
                                String shares = sp.getString(ticker, "0");
                                if (shares.equals("0")) {
                                    ownInfo.setText("You have 0 shares of " + ticker + ".\nStart Trading!");
                                } else {
                                    ownInfo.setText("Shares owned: " + shares + "\nMarket Value: $" + new BigDecimal(shares).multiply(new BigDecimal(jsonObject.getString("last"))).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
                                }
                                curPrice = jsonObject.getString("last");
                                flag1 = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> Log.e("VOLLEY", error.getMessage()));
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    private void getDetail(String ticker) {
        String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/details/" + ticker;
        final TextView tickerView = findViewById(R.id.detail_ticker);
        final TextView nameView = findViewById(R.id.detail_name);
        final TextView descView = findViewById(R.id.detail_about_content);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        response -> {
                            try {
                                tickerView.setText(response.getString("ticker"));
                                nameView.setText(response.getString("name"));
                                descView.setText(response.getString("description"));
                                companyName = response.getString("name");
                                if (sp.getStringSet("favorites", new LinkedHashSet<>()).contains(ticker + "," + companyName))
                                    findViewById(R.id.not_click_fav).setVisibility(View.GONE);
                                else findViewById(R.id.clicked_fav).setVisibility(View.GONE);
                                flag2 = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> Log.e("VOLLEY", "getDetail went wrong"));
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void getNews(String ticker) {
        String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/news/" + ticker;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null,
                        response -> {
                            try {
                                JSONArray articles = response.getJSONArray("articles");
                                ArrayList<NewsItem> list = new ArrayList<>();
                                String[][] newsContent = new String[articles.length()][5];
                                JSONObject first = articles.getJSONObject(0);
                                ImageView firstImage = findViewById(R.id.news_first_image);
                                TextView firstPublisher = findViewById(R.id.news_first_publisher);
                                TextView firstTime = findViewById(R.id.news_first_time);
                                TextView firstTitle = findViewById(R.id.news_first_title);
                                firstURL = first.getString("url");
                                firstImageURL = first.getString("urlToImage");
                                this.firstTitle = first.getString("title");
                                Picasso.with(this).load(first.getString("urlToImage")).transform(new RoundedCornersTransformation(40, 0)).error(getDrawable(R.drawable.no_image)).fit().into(firstImage);
                                firstPublisher.setText(first.getJSONObject("source").getString("name"));
                                firstTime.setText(getFormattedTime(first.getString("publishedAt")));
                                firstTitle.setText(first.getString("title"));
                                for (int i = 1; i < articles.length(); i++) {
                                    NewsItem newsItem = new NewsItem(articles.getJSONObject(i).getString("urlToImage"),
                                            articles.getJSONObject(i).getString("title"),
                                            articles.getJSONObject(i).getString("publishedAt"),
                                            articles.getJSONObject(i).getString("url"),
                                            articles.getJSONObject(i).getJSONObject("source").getString("name"));
                                    list.add(newsItem);
                                }
                                newsView = findViewById(R.id.detail_news_content);
                                newsView.setLayoutManager(new LinearLayoutManager(this));
                                newsView.setAdapter(new NewsCardsAdapter(this, list));
                                flag3 = true;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> Log.e("VOLLEY", error.getMessage()));
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    private void getHist(String ticker) {
        String url = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/hist/" + ticker + "/2018-12-03";
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null,
                        response -> {
                            try {
                                for (int i = 0; i < response.length(); i++) {
                                    chartData += ZonedDateTime.parse(response.getJSONObject(i).getString("date"), DateTimeFormatter.ISO_DATE_TIME)
                                            .withZoneSameInstant(ZoneId.systemDefault()).toInstant().toEpochMilli() + ",";
                                    chartData += response.getJSONObject(i).getString("open") + ",";
                                    chartData += response.getJSONObject(i).getString("high") + ",";
                                    chartData += response.getJSONObject(i).getString("low") + ",";
                                    chartData += response.getJSONObject(i).getString("close") + ",";
                                    chartData += response.getJSONObject(i).getString("volume") + ",,";
                                }
                                highchart.setWebViewClient(new WebViewClient() {
                                    public void onPageFinished(WebView view, String url) {
                                        highchart.loadUrl("javascript:setData('" + chartData.substring(0, chartData.length() - 2) + "')");
                                    }
                                });
                                highchart.loadUrl("file:///android_asset/stock_chart.html");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }, error -> Log.e("VOLLEY", error.getMessage()));
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public void showMore(View view) {
        findViewById(R.id.detail_show_more).setVisibility(View.GONE);
        TextView content = findViewById(R.id.detail_about_content);
        content.setMaxLines(Integer.MAX_VALUE);
        findViewById(R.id.detail_show_less).setVisibility(View.VISIBLE);
    }

    public void showLess(View view) {
        findViewById(R.id.detail_show_less).setVisibility(View.GONE);
        TextView content = findViewById(R.id.detail_about_content);
        content.setMaxLines(2);
        findViewById(R.id.detail_show_more).setVisibility(View.VISIBLE);
    }

    public void showTradeDialog(View view) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.trade_dialog);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        //Window window = dialog.getWindow();
        //window.setLayout(1200, 1200);

        TextView title = dialog.findViewById(R.id.trade_title);
        EditText input = dialog.findViewById(R.id.trade_input);
        TextView equation = dialog.findViewById(R.id.trade_equation);
        TextView sum = dialog.findViewById(R.id.trade_sum);
        Button buy = dialog.findViewById(R.id.trade_buy);
        Button sell = dialog.findViewById(R.id.trade_sell);

        title.setText("Trade " + companyName + " shares");
        equation.setText("0 x $" + curPrice + "/share = $0.00");
        sum.setText("$" + sp.getString("networth", "0") + " available to buy " + ticker);

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputNumber = input.getText().toString();
                String share = inputNumber.length() > 0 ? inputNumber : "0";
                String remain = sp.getString("networth", "0");
                String originShare = sp.getString(ticker, "0");
                double totalPrice = new BigDecimal(share).multiply(new BigDecimal(curPrice)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                if (new BigDecimal(remain).subtract(BigDecimal.valueOf(totalPrice)).doubleValue() < 0)
                    Toast.makeText(context, "Not enough money to buy", Toast.LENGTH_LONG).show();
                else if (Double.parseDouble(share) <= 0)
                    Toast.makeText(context, "Cannot buy less than 0 shares", Toast.LENGTH_LONG).show();
                else {
                    String newRemain = new BigDecimal(remain).subtract(new BigDecimal(share).multiply(new BigDecimal(curPrice))).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    sp.edit().putString("networth", newRemain).apply();

                    Set<String> port = new LinkedHashSet<>(sp.getStringSet("portfolio", new LinkedHashSet<>()));
                    port.add(ticker + "," + companyName);
                    sp.edit().putStringSet("portfolio", port).apply();

                    String newShare = new BigDecimal(originShare).add(new BigDecimal(share)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    sp.edit().putString(ticker, newShare).apply();

                    dialog.dismiss();
                    Dialog congra = new Dialog(context);
                    congra.setContentView(R.layout.trade_success);
                    congra.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView congraText = congra.findViewById(R.id.success_text);
                    Button closeCongra = congra.findViewById(R.id.success_done);
                    congraText.setText("You have successfully bought " + share + " shares of " + ticker);
                    closeCongra.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            congra.dismiss();
                        }
                    });
                    congra.show();
                }
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputNumber = input.getText().toString();
                String share = inputNumber.length() > 0 ? inputNumber : "0";
                String remain = sp.getString("networth", "0");
                String originShare = sp.getString(ticker, "0");
                double totalPrice = new BigDecimal(share).multiply(new BigDecimal(curPrice)).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();

                if (new BigDecimal(originShare).subtract(new BigDecimal(share)).doubleValue() < 0)
                    Toast.makeText(context, "Not enough shares to sell", Toast.LENGTH_LONG).show();
                else if (Double.parseDouble(share) <= 0)
                    Toast.makeText(context, "Cannot sell less than 0 shares", Toast.LENGTH_LONG).show();
                else {
                    String newRemain = new BigDecimal(remain).add(new BigDecimal(share).multiply(new BigDecimal(curPrice))).toString();
                    sp.edit().putString("networth", newRemain).apply();

                    String newShare = new BigDecimal(originShare).subtract(new BigDecimal(share)).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                    if (Double.parseDouble(newShare) <= 0) {
                        sp.edit().remove(ticker).apply();
                        Set<String> port = new LinkedHashSet<>(sp.getStringSet("portfolio", new LinkedHashSet<>()));
                        port.remove(ticker + "," + companyName);
                        sp.edit().putStringSet("portfolio", port).apply();
                    } else {
                        sp.edit().putString(ticker, newShare).apply();
                    }

                    dialog.dismiss();
                    Dialog congra = new Dialog(context);
                    congra.setContentView(R.layout.trade_success);
                    congra.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    TextView congraText = congra.findViewById(R.id.success_text);
                    Button closeCongra = congra.findViewById(R.id.success_done);
                    congraText.setText("You have successfully sold " + share + " shares of " + ticker);
                    closeCongra.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            congra.dismiss();
                        }
                    });
                    congra.show();
                }
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    double curShares = new BigDecimal(s.toString()).doubleValue();
                    equation.setText(s.toString() + " x $" + curPrice + "/share = $" + BigDecimal.valueOf(curShares).multiply(new BigDecimal(curPrice)).setScale(4, BigDecimal.ROUND_HALF_UP).toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        dialog.show();
    }

    public void addFav(View view) {
        Set<String> set = new LinkedHashSet<>(sp.getStringSet("favorites", new LinkedHashSet<>()));
        set.add(ticker + "," + companyName);
        sp.edit().putStringSet("favorites", set).apply();
        findViewById(R.id.not_click_fav).setVisibility(View.GONE);
        findViewById(R.id.clicked_fav).setVisibility(View.VISIBLE);
        Toast.makeText(this, "\"" + ticker + "\" was added to favorites", Toast.LENGTH_LONG).show();
    }

    public void removeFav(View view) {
        Set<String> set = new LinkedHashSet<>(sp.getStringSet("favorites", new LinkedHashSet<>()));
        set.remove(ticker);
        sp.edit().putStringSet("favorites", set).apply();
        findViewById(R.id.clicked_fav).setVisibility(View.GONE);
        findViewById(R.id.not_click_fav).setVisibility(View.VISIBLE);
        Toast.makeText(this, "\"" + ticker + "\" was removed from favorites", Toast.LENGTH_LONG).show();
    }

    private void stopSpinner() {
        if (flag1 && flag2 && flag3) {
            spinner.setVisibility(View.GONE);
            spinnerText.setVisibility(View.GONE);
            detailPage.setVisibility(View.VISIBLE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getFormattedTime(String oTime) {
        String str = "";
        String suffix = "ago";

        Date nowTime = new Date();
        Instant instant = Instant.parse(oTime);
        ZoneId z = ZoneId.of("America/Los_Angeles");
        ZonedDateTime zdt = instant.atZone(z);
        Date newsTime = Date.from(zdt.toInstant());
        long dateDiff = nowTime.getTime() - newsTime.getTime();
        long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
        long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
        long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
        long day = TimeUnit.MILLISECONDS.toDays(dateDiff);
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