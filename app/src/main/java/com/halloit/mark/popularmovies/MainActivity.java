package com.halloit.mark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

import com.halloit.mark.popularmovies.MovieContract.MovieEntry;

import static com.halloit.mark.popularmovies.BuildConfig.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

// TODO optimize for reuse;
// cache API json results;
// reuse adapter when switching view type

// TODO add favorites menu option
// TODO add favorites main display retrieval


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity.java";
    private Boolean isPopular = true;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mErrorView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getContentResolver().delete(MovieEntry.CONTENT_URI, null, null);
        setContentView(R.layout.activity_main);
//        Log.i(TAG, "MyTheMovieDBApiKeyV4: " + THE_MOVIE_DB_API_KEY_V4);
        Log.i(TAG, "MyTheMovieDBApiKeyV3: " + THE_MOVIE_DB_API_KEY_V3);
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
        new FetchMovieListTask().execute(isPopular);
    }

    private class FetchMovieListTask extends AsyncTask<Boolean, Void, String> {
        private boolean isPop = true;
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "preparing to retrieve");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Boolean... isPop) {
            if (! Utils.isInternetAvailable()) {
                return null;
            }
            Log.i(TAG, "urlCodes length: " + isPop.length);
            Log.i(TAG, "urlCodes[0]: " + isPop[0].toString());
            this.isPop = isPop[0];
            String jsonMovieList = null;
            try {
                URL url = new URL(POPULAR_BASE_URL + THE_MOVIE_DB_API_KEY_V3);
                if (! this.isPop) {
                    url = new URL(TOPRATED_BASE_URL + THE_MOVIE_DB_API_KEY_V3);
                }
                jsonMovieList = Utils.getResponseFromHttpUrl(url);
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
                JSONObject moviesJ = new JSONObject(moviesData);
                JSONArray results = moviesJ.getJSONArray("results");
                int len = results.length();
                ContentValues[] values = new ContentValues[len];
                for (int i = 0; i < len; i++) {
                    JSONObject movieJ = results.getJSONObject(i);
                    Log.i(TAG, movieJ.toString(2));
                    String posterPath = movieJ.getString("poster_path");
                    Log.i(TAG, "image path: " + posterPath);
                    values[i] = new ContentValues();
                    values[i].put(MovieEntry.COLUMN_IMAGE_FULL_PATH,
                            IMAGE_BASE_URL + "w185" + posterPath);
                    values[i].put(MovieEntry.COLUMN_POSTER_PATH, posterPath);
                    values[i].put(MovieEntry.COLUMN_BACKDROP_PATH,
                            movieJ.getString("backdrop_path"));
                    values[i].put(MovieEntry.COLUMN_MOVIE_ID, movieJ.getLong("id"));
                    values[i].put(MovieEntry.COLUMN_TITLE, movieJ.getString("title"));
                    values[i].put(MovieEntry.COLUMN_ORIGINAL_LANGUAGE,
                            movieJ.getString("original_language"));
                    values[i].put(MovieEntry.COLUMN_ORIGINAL_TITLE,
                            movieJ.getString("original_title"));
                    values[i].put(MovieEntry.COLUMN_OVERVIEW, movieJ.getString("overview"));
                    values[i].put(MovieEntry.COLUMN_RELEASE_DATE, movieJ.getString("release_date"));
                    values[i].put(MovieEntry.COLUMN_VOTE_AVERAGE, movieJ.getDouble("vote_average"));
                    if (this.isPop) {
                        values[i].put(MovieEntry.COLUMN_POP_PRIORITY, i + 1);
                        values[i].put(MovieEntry.COLUMN_TR_PRIORITY, 0);
                    } else {
                        values[i].put(MovieEntry.COLUMN_TR_PRIORITY, i + 1);
                        values[i].put(MovieEntry.COLUMN_POP_PRIORITY, 0);
                    }
                }
                Uri uri = MovieEntry.CONTENT_URI;
                getContentResolver().bulkInsert(uri, values);
                mGridView.setAdapter(new ImageMainAdapter(MainActivity.this, this.isPop));
                mProgressBar.setVisibility(View.INVISIBLE);
                mGridView.setVisibility(View.VISIBLE);
            } catch (JSONException e) {
                Log.i(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }
}
