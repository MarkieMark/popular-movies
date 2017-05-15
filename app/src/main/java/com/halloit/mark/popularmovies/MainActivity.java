package com.halloit.mark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        LoaderManager.LoaderCallbacks<Utils.BooleanString>, SharedPreferences.OnSharedPreferenceChangeListener {

    private enum DisplayType {POPULAR, TOP_RATED, FAVORITES}
    private static final String TAG = "MainActivity";
    private static final String KEY_MOVIE_LIST_IS_FAVORITE = "movie_list_favorite";
    private static final String KEY_MOVIE_LIST_IS_POPULAR = "movie_list_popular";
    private static final String KEY_OFFSET = "offset";
    private static final int MOVIE_DB_MAIN_SEARCH_LOADER = 712;
    private static DisplayType displayType = DisplayType.POPULAR;
    private int lengthLimit = 100;
    private GridView mGridView;
    private ProgressBar mProgressBar;
    private TextView mErrorView;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (getString(R.string.pref_number_key).equals(key)) {
            lengthLimit = Integer.valueOf(sharedPreferences.getString(key,
                    getString(R.string.pref_number_default)));
        }
    }

    @Override
    public Loader<Utils.BooleanString> onCreateLoader(int id, final Bundle args) {
        final boolean isFav = args.getBoolean(KEY_MOVIE_LIST_IS_FAVORITE);
        final boolean isPop = args.getBoolean(KEY_MOVIE_LIST_IS_POPULAR);
        final int offset = args.getInt(KEY_OFFSET);
        return new AsyncTaskLoader<Utils.BooleanString>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Utils.BooleanString loadInBackground() {
                Utils.BooleanString ret = new Utils().new BooleanString(isFav, isPop, offset);
                if ((isFav) || (! Utils.isInternetAvailable())) {
                    return ret;
                }
                try {
                    String urlString = POPULAR_BASE_URL + THE_MOVIE_DB_API_KEY_V3;
                    if (!isPop) {
                        urlString = TOPRATED_BASE_URL + THE_MOVIE_DB_API_KEY_V3;
                    }
                    if (offset > 0) urlString += URL_ADD_PAGE +
                            String.valueOf(1 + offset / 20);
                    URL url = new URL(urlString);
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
            Cursor c;
            String selectCol;
            if (isPop) {
                selectCol = MovieEntry.COLUMN_POP_PRIORITY;
            } else {
                selectCol = MovieEntry.COLUMN_TR_PRIORITY;
            }
            c = getContentResolver().query(MovieEntry.CONTENT_URI,
                    null, selectCol + " > 0 ", null, null);
            if (c != null) {
                if (c.getCount() < 1) {
                    noDbData();
                } else {
                    mGridView.setAdapter(new ImageMainAdapter(MainActivity.this, isPop, false));
                    mProgressBar.setVisibility(View.INVISIBLE);
                    mGridView.setVisibility(View.VISIBLE);
                }
                c.close();
                return;
            }
            noDbData();
            return;
        }
        Log.i(TAG, moviesData.jsonString);
        try {
            JSONObject moviesJ = new JSONObject(moviesData.jsonString);
            JSONArray results = moviesJ.getJSONArray("results");
            int len = results.length();
            if ((len == 20) && (moviesData.offset < lengthLimit)) {
                DisplayType type = DisplayType.TOP_RATED;
                if (moviesData.isPop) {
                    type = DisplayType.POPULAR;
                }
                loadMoviesData(type, moviesData.offset + 20);
            }
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
                if (isPop) {
                    values[i].put(MovieEntry.COLUMN_POP_PRIORITY, i + 1 + moviesData.offset);
                    values[i].put(MovieEntry.COLUMN_TR_PRIORITY, 0);
                } else {
                    values[i].put(MovieEntry.COLUMN_TR_PRIORITY, i + 1 + moviesData.offset);
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
        lengthLimit = Integer.valueOf(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(getString(R.string.pref_number_key),
                getString(R.string.pref_number_default)));
        Log.i(TAG, "loading movies data");
        loadMoviesData(displayType, 0);

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
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(getString(R.string.pref_remember_type_key),
                getResources().getBoolean(R.bool.pref_remember_type_default))) {
            int pos = sharedPreferences.getInt(getString(R.string.pref_type), 0);
            spinner.setSelection(pos);
            displayType = DisplayType.values()[pos];
        }
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
        displayType = DisplayType.values()[pos];
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean rememberType = sharedPreferences.getBoolean(
                getString(R.string.pref_remember_type_key),
                getResources().getBoolean(R.bool.pref_remember_type_default));
        if (rememberType) {
            SharedPreferences.Editor prefEditor = sharedPreferences.edit();
            prefEditor.putInt(getString(R.string.pref_type), pos);
            prefEditor.apply();
        }
        loadMoviesData(displayType, 0);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    private void loadMoviesData(DisplayType type, int offset) {
        if (offset == 0) {
            mGridView.setVisibility(View.INVISIBLE);
            mErrorView.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_MOVIE_LIST_IS_FAVORITE, type == DisplayType.FAVORITES);
        bundle.putBoolean(KEY_MOVIE_LIST_IS_POPULAR, type == DisplayType.POPULAR);
        bundle.putInt(KEY_OFFSET, offset);
        LoaderManager manager = getSupportLoaderManager();
        Loader<String> loader = manager.getLoader(MOVIE_DB_MAIN_SEARCH_LOADER + offset);
        if (loader == null) {
            manager.initLoader(MOVIE_DB_MAIN_SEARCH_LOADER + offset, bundle, this);
        } else {
            manager.restartLoader(MOVIE_DB_MAIN_SEARCH_LOADER + offset, bundle, this);
        }
    }

    private void noFavorites() {
        mErrorView.setText(R.string.no_favorites);
        mErrorView.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void noDbData() {
        if (!Utils.isNetworkConnected(this)) {
            mErrorView.setText(R.string.no_network);
        } else if (!Utils.isInternetAvailable()) {
            mErrorView.setText(R.string.no_connection);
        } else {
            mErrorView.setText(R.string.no_data);
        }
        mErrorView.setVisibility(View.VISIBLE);
        mGridView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() displayType " + displayType.name());
        if (displayType == DisplayType.FAVORITES) { // Favorites may have changed
            loadMoviesData(displayType, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
