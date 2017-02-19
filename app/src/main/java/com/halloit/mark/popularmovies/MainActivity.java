package com.halloit.mark.popularmovies;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

// TODO optimize for reuse;
// cache API json results;
// reuse adapter when switching view type


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity.java";
    private Boolean isPopular = true;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mErrorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "MyTheMovieDBApiKeyV4: " + BuildConfig.THE_MOVIE_DB_API_KEY_V4);
        Log.i(TAG, "MyTheMovieDBApiKeyV3: " + BuildConfig.THE_MOVIE_DB_API_KEY_V3);
        mGridView = (GridView) findViewById(R.id.main_grid_view);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_indicator);
        mErrorView = (TextView) findViewById(R.id.tv_error);
        Log.i(TAG, "loading movies data");
        loadMoviesData();

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, (String) v.getTag());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(
                menu.findItem(R.id.menu_spinner));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter
                .createFromResource(this, R.array.sort_strings,
                        android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        if (parent.getItemAtPosition(pos).equals("Top Rated")) {
            Log.i(TAG, "selection: Top Rated");
            if (isPopular) {
                isPopular = false;
                loadMoviesData();
            }
        }
        if (parent.getItemAtPosition(pos).equals("Popular")) {
            Log.i(TAG, "selection: Popular");
            if (!isPopular) {
                isPopular = true;
                loadMoviesData();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void loadMoviesData() {
        if (! Utils.isNetworkConnected(this)) {
            mErrorView.setText(R.string.no_network);
            mErrorView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        mGridView.setVisibility(View.INVISIBLE);
        mErrorView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        String baseURL;
        if (isPopular) {
            baseURL = BuildConfig.POPULAR_BASE_URL;
        } else {
            baseURL = BuildConfig.TOPRATED_BASE_URL;
        }
        URL url = null;
        try {
            url = new URL(baseURL + BuildConfig.THE_MOVIE_DB_API_KEY_V3);
            Log.i(TAG, "url: " + url.toString());
        } catch (MalformedURLException e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
        new FetchMovieListTask().execute(url);
    }

    class FetchMovieListTask extends AsyncTask<URL, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "preparing to retrieve");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... urls) {
            if (! Utils.isInternetAvailable()) {
                return null;
            }
            Log.i(TAG, "urls length: " + urls.length);
            Log.i(TAG, "urls[0]: " + urls[0].toString());
            String jsonMovieList = null;
            try {
                jsonMovieList = Utils.getResponseFromHttpUrl(urls[0]);
                Log.i(TAG, "jsonMovieList: " + jsonMovieList);
            } catch (IOException e) {
                Log.i(TAG, e.toString());
                e.printStackTrace();
            }
            return jsonMovieList;
        }

        @Override
        protected void onPostExecute(String moviesData) {
            if (moviesData == null) {
                mErrorView.setText(R.string.no_connection);
                mErrorView.setVisibility(View.VISIBLE);
                mGridView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                return;
            }
            Log.i(TAG, moviesData);
            try {
                JSONObject movies = new JSONObject(moviesData);
                JSONArray results = movies.getJSONArray("results");
                int len = results.length();
                String[] imagePaths = new String[len];
                for (int i = 0; i < len; i++) {
                    JSONObject movie = results.getJSONObject(i);
                    String posterPath = movie.getString("poster_path");
                    Log.i(TAG, "image path: " + posterPath);
                    imagePaths[i] = BuildConfig.IMAGE_BASE_URL + "w185" + posterPath;
                }
                mGridView.setAdapter(new ImageMainAdapter(MainActivity.this, imagePaths));
                mProgressBar.setVisibility(View.INVISIBLE);
                mGridView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                Log.i(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }
}
