package com.example.stocksearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = "com.example.stocksearch.MESSAGE";
    private RecyclerView recyclerViewPortfolio;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private static ProgressBar spinner;
    private static TextView spinnerText;
    private static NestedScrollView homePage;
    private Context context;
    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        spinner = findViewById(R.id.home_progressBar);
        spinner.setVisibility(View.VISIBLE);
        spinnerText = findViewById(R.id.home_progressBar_text);
        (homePage = findViewById(R.id.home_page)).setVisibility(View.GONE);
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        TextView dateView = findViewById(R.id.textView2);
        dateView.setText("  " + new SimpleDateFormat("MMMM dd, yyyy").format(Calendar.getInstance().getTime()));
        //recyclerViewPortfolio = (RecyclerView) findViewById(R.id.recycler_view_portfolio);
        //recyclerViewPortfolio.setHasFixedSize(true); // fixed size ?
        //layoutManager = new GridLayoutManager(this, 2);
        //recyclerViewPortfolio.setLayoutManager(layoutManager);
        //recyclerViewPortfolio.setAdapter(mAdapter = new MyAdapter(new String[]{"1","2","3","4"}));
        //recyclerViewPortfolio.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frameLayout_1, new Example1Fragment()).commit();

        TextView footer = findViewById(R.id.home_footer);
        footer.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiingo.com/"));
            this.startActivity(intent);
        });
        context = this;

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d("UPDATE", "Fetched data from endpoint.");
                handler.postDelayed(this, 15000);
            }
        }, 15000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem searchInput = menu.findItem(R.id.action_search_input);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchInput);
        SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        AtomicBoolean canSubmit = new AtomicBoolean(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (canSubmit.get()) sendMessage(query.split(" - ")[0], searchView);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 2) {
                    String urlAuto = "http://nodejsapp-env.eba-djt2phnu.us-east-1.elasticbeanstalk.com/auto/" + newText;
                    JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                            (Request.Method.GET, urlAuto, null,
                                    response -> {
                                        List<String> companyList = new ArrayList<>();
                                        for (int i = 0; i < response.length(); i++) {
                                            try {
                                                JSONObject jsonObject = response.getJSONObject(i);
                                                String curTicker = jsonObject.getString("ticker");
                                                String companyName = jsonObject.getString("name");
                                                companyList.add(curTicker + " - " + companyName);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        ArrayAdapter<String> companyAdapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, companyList);
                                        searchAutoComplete.setAdapter(companyAdapter);
                                        companyAdapter.notifyDataSetChanged();
                                        searchAutoComplete.showDropDown();
                                    }, error -> Log.e("VOLLEY", "getAuto went wrong"));
                    MySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(jsonArrayRequest);
                } else {
                    searchAutoComplete.setAdapter(new ArrayAdapter(context, android.R.layout.simple_dropdown_item_1line));
                }
                return false;
            }
        });
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            String tickerAndName = (String) parent.getItemAtPosition(position);
            canSubmit.set(true);
            searchView.setQuery(tickerAndName, false);
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void sendMessage(String query, View view) {
        handler.removeCallbacksAndMessages(null);
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        intent.putExtra(EXTRA_MESSAGE, query); // key and value
        startActivity(intent);
    }

    public static void stopSpinner() {
        spinner.setVisibility(View.GONE);
        spinnerText.setVisibility(View.GONE);
        homePage.setVisibility(View.VISIBLE);
    }

    public static void stopHandler() {
        handler.removeCallbacksAndMessages(null);
    }

}