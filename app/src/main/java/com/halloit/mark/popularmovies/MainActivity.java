package com.halloit.mark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
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
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

// TODO optimize for reuse;
// reuse adapter when switching view type



public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Utils.BooleanString> {
    private enum DisplayType {POPULAR, TOP_RATED, FAVORITES}
    private static final String TAG = "MainActivity";
    private static final String KEY_MOVIE_LIST_IS_FAVORITE = "movie_list_favorite";
    private static final String KEY_MOVIE_LIST_IS_POPULAR = "movie_list_popular";
    private static final int MOVIE_DB_MAIN_SEARCH_LOADER = 712;
    private static DisplayType displayType = DisplayType.POPULAR;
    // TODO make selection sticky / preference
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mErrorView;

    @Override
    public Loader<Utils.BooleanString> onCreateLoader(int id, final Bundle args) {
        final boolean isFav = args.getBoolean(KEY_MOVIE_LIST_IS_FAVORITE);
        final boolean isPop = args.getBoolean(KEY_MOVIE_LIST_IS_POPULAR);
        return new AsyncTaskLoader<Utils.BooleanString>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Utils.BooleanString loadInBackground() {
                Utils.BooleanString ret = new Utils().new BooleanString(isFav, isPop);
                if ((isFav) || (! Utils.isInternetAvailable())) {
                    return ret;
                }
                try {
                    URL url = new URL(POPULAR_BASE_URL + THE_MOVIE_DB_API_KEY_V3);
                    if (!isPop) {
                        url = new URL(TOPRATED_BASE_URL + THE_MOVIE_DB_API_KEY_V3);
                    }
                    ret.jsonString = Utils.getResponseFromHttpUrl(url);
                    Log.i(TAG, "jsonMovieList: " + ret.jsonString);
                } catch (IOException e) {
                    Log.i(TAG, e.toString());
                    e.printStackTrace();
                }
                return ret;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Utils.BooleanString> loader, Utils.BooleanString moviesData) {
        boolean isFav = moviesData.isFav;
        boolean isPop = moviesData.isPop;
        if (isFav) {
            Cursor c = getContentResolver().query(MovieEntry.CONTENT_URI,
                    null, MovieEntry.COLUMN_FAVORITE + " = 1 ", null, null);
            if (c != null) {
                if (c.getCount() < 1) {
                    noFavorites();
                } else {
                    mGridView.setAdapter(new ImageMainAdapter(MainActivity.this, isPop, true));
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mGridView.setVisibility(View.VISIBLE);
                }
                c.close();
                return;
            }
            noFavorites();
            return;
        }
        if (moviesData.jsonString == null) {
            mErrorView.setText(R.string.no_connection);
            mErrorView.setVisibility(View.VISIBLE);
            mGridView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.INVISIBLE);
            return;
        }
        Log.i(TAG, moviesData.jsonString);
        try {
            JSONObject moviesJ = new JSONObject(moviesData.jsonString);
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
                // TODO handle retrieval of more than 20 movies
                if (isPop) {
                    values[i].put(MovieEntry.COLUMN_POP_PRIORITY, i + 1);
                    values[i].put(MovieEntry.COLUMN_TR_PRIORITY, 0);
                } else {
                    values[i].put(MovieEntry.COLUMN_TR_PRIORITY, i + 1);
                    values[i].put(MovieEntry.COLUMN_POP_PRIORITY, 0);
                }
            }
            Uri uri = MovieEntry.CONTENT_URI;
            getContentResolver().bulkInsert(uri, values);
            mGridView.setAdapter(new ImageMainAdapter(MainActivity.this, isPop, false));
            mProgressBar.setVisibility(View.INVISIBLE);
            mGridView.setVisibility(View.VISIBLE);
        } catch (Exception E) {
            Log.i(TAG, E.toString());
            E.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Utils.BooleanString> loader) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getContentResolver().delete(MovieEntry.CONTENT_URI, null, null);
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
        if (parent.getItemAtPosition(pos).equals("Popular")) {
            Log.i(TAG, "selection: Popular");
            displayType = DisplayType.POPULAR;
        }
        if (parent.getItemAtPosition(pos).equals("Top Rated")) {
            Log.i(TAG, "selection: Top Rated");
            displayType = DisplayType.TOP_RATED;
        }
        if (parent.getItemAtPosition(pos).equals("Favorites")) {
            displayType = DisplayType.FAVORITES;
        }
        loadMoviesData();
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
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_MOVIE_LIST_IS_FAVORITE, displayType == DisplayType.FAVORITES);
        bundle.putBoolean(KEY_MOVIE_LIST_IS_POPULAR, displayType == DisplayType.POPULAR);
        LoaderManager manager = getSupportLoaderManager();
        Loader<String> loader = manager.getLoader(MOVIE_DB_MAIN_SEARCH_LOADER);
        if (loader == null) {
            manager.initLoader(MOVIE_DB_MAIN_SEARCH_LOADER, bundle, this);
        } else {
            manager.restartLoader(MOVIE_DB_MAIN_SEARCH_LOADER, bundle, this);
        }
    }

    private void noFavorites() {
        mErrorView.setText(R.string.no_favorites);
        mErrorView.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (displayType == DisplayType.FAVORITES) { // Favorites may have changed
            loadMoviesData();
        }
    }
}
