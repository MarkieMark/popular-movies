package com.halloit.mark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static com.halloit.mark.popularmovies.BuildConfig.*;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {
    private static final String TAG = "DetailActivity.java";
    private LinearLayout mTrailerView;
    private WebView mReviewView;
    private Long movieId;
    private CheckBox mFavoriteButton;

    private static final String[] COLUMN_PROJECTION = {MovieEntry._ID, MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IMAGE_FULL_PATH, MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE, MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW, MovieEntry.COLUMN_FAVORITE};
//    private static final int IND_COLUMN__ID = 0;
//    private static final int IND_COLUMN_MOVIE_ID = 1;
    private static final int IND_COLUMN_IMAGE_FULL_PATH = 2;
    private static final int IND_COLUMN_TITLE = 3;
    private static final int IND_COLUMN_RELEASE_DATE = 4;
    private static final int IND_COLUMN_VOTE_AVERAGE = 5;
    private static final int IND_COLUMN_OVERVIEW = 6;
    private static final int IND_COLUMN_FAVORITE = 7;

    private static final String KEY_MOVIE_ID = "Movie_ID";
    private static final int MOVIEDB_DETAIL_SEARCH_LOADER = 711;

// TODO spare imageview for spare image
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ImageView mImageView = (ImageView) findViewById(R.id.dv_image);
        ProgressBar mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_detail);
        TextView mErrorView = (TextView) findViewById(R.id.tv_error_detail);
        LinearLayout mLinearLayoutMain = (LinearLayout) findViewById(R.id.ll_detail_main);
        TextView mTitleView = (TextView) findViewById(R.id.tv_detail_title);
        TextView mDateView = (TextView) findViewById(R.id.tv_detail_date);
        TextView mScoreView = (TextView) findViewById(R.id.tv_detail_score);
        WebView mDescriptionView = (WebView) findViewById(R.id.tv_detail_description);
        mFavoriteButton = (CheckBox) findViewById(R.id.ib_favorite);
        mReviewView = (WebView) findViewById(R.id.wv_reviews);
        mTrailerView = (LinearLayout) findViewById(R.id.ll_trailer);
        Intent intent = getIntent();
        getSupportLoaderManager().initLoader(MOVIEDB_DETAIL_SEARCH_LOADER, null, this);
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            if (! Utils.isNetworkConnected(this)) {
                mErrorView.setText(R.string.no_network);
                mErrorView.setVisibility(View.VISIBLE);
//                mImageView.setVisibility(View.INVISIBLE);
                mLinearLayoutMain.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                return;
            }
            // TODO check for actual connection
            String content = intent.getStringExtra(Intent.EXTRA_TEXT);
            movieId = Long.valueOf(content);
            Log.i(TAG, "intent content: " + content);
            Log.i(TAG, "movie details: " + movieId);
            mImageView.setMinimumHeight(500);
            mImageView.setMinimumWidth(325);
            Cursor c = getContentResolver().query(MovieEntry.buildMovieUriWithId(movieId),
                    COLUMN_PROJECTION, null, null, null);
            if (c == null) return;
            if (c.getCount() < 1) {
                c.close();
                return;
            }
            c.moveToFirst();

            Picasso.with(this).load(c.getString(IND_COLUMN_IMAGE_FULL_PATH)).into(mImageView);
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_MOVIE_ID, movieId);
            LoaderManager manager = getSupportLoaderManager();
            Loader<String> loader = manager.getLoader(MOVIEDB_DETAIL_SEARCH_LOADER);
            if (loader == null) {
                manager.initLoader(MOVIEDB_DETAIL_SEARCH_LOADER, bundle, this);
            } else {
                manager.restartLoader(MOVIEDB_DETAIL_SEARCH_LOADER, bundle, this);
            }
//            new FetchMovieDetailsTask().execute(movieId);
            mTitleView.setText(c.getString(IND_COLUMN_TITLE));
            mDateView.setText(c.getString(IND_COLUMN_RELEASE_DATE).substring(0,4));
            mScoreView.setText(getString(R.string.vote_score, c.getFloat(IND_COLUMN_VOTE_AVERAGE)));
            mDescriptionView.loadData(getString(R.string.html_prefix) +
                    c.getString(IND_COLUMN_OVERVIEW) + getString(R.string.html_suffix),
                    "text/html; charset=utf-8", "utf-8");
            mFavoriteButton.setChecked(c.getInt(IND_COLUMN_FAVORITE) > 0);
            c.close();
        }
    }

    public void toggleFavorite(View view) {
        Log.i(TAG, "toggleFavorite()");
        boolean isFavorite = mFavoriteButton.isChecked();
        ContentValues value = new ContentValues();
        String[] selectionArgs = new String[]{movieId.toString()};
        value.put(MovieEntry.COLUMN_FAVORITE, isFavorite ? 1 : 0);
        int numberOfRows = getContentResolver().update(MovieEntry.buildMovieUriWithId(movieId),
                value, MovieEntry.COLUMN_MOVIE_ID + " = ? ", selectionArgs);
        if (numberOfRows < 1) {
            mFavoriteButton.setChecked(!isFavorite);
            Toast toast = Toast.makeText(this, "Error" + (isFavorite ? "Adding to" : "Removing From")
                    + " Favorites", Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
            return;
        }
        Cursor c = getContentResolver().query(MovieEntry.buildMovieUriWithId(movieId),
                COLUMN_PROJECTION, null, null, null);
        if (c == null) return;
        if (c.getCount() < 1) {
            c.close();
            return;
        }
        c.moveToFirst();
        String title = c.getString(IND_COLUMN_TITLE);
        Toast toast = Toast.makeText(this, title + "\n" + (isFavorite ? "Added to" : "Removed From")
                + " Favorites", Toast.LENGTH_LONG);
        TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
        if( v != null) v.setGravity(Gravity.CENTER);
        toast.show();
        c.close();
    }

    @Override
    public Loader<String> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String>(this) {
            @Override
            protected void onStartLoading() {
//                super.onStartLoading();
                forceLoad();
            }

            @Override
            public String loadInBackground() {
                if (! Utils.isInternetAvailable()) {
                    return null;
                }
                movieId = args.getLong(KEY_MOVIE_ID);
                Log.i(TAG, "movieId " + movieId);
                String jsonMovieDetails = null;
                try {
                    // TODO URL for videos /movie/{id}/videos
                    // TODO URL for reviews /movie/{id}/reviews
                    URL url = new URL(MOVIE_DETAIL_URL + movieId +
                            URL_TRAILER_Q + THE_MOVIE_DB_API_KEY_V3);
                    Log.i(TAG, "url: " + url.toString());
                    jsonMovieDetails = Utils.getResponseFromHttpUrl(url);
                    Log.i(TAG, "jsonMovieDetails: " + jsonMovieDetails);
                } catch (Exception e) {
                    Log.i(TAG, e.toString());
                    e.printStackTrace();
                }
                return jsonMovieDetails;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String movieDetails) {
        if (movieDetails == null) {
            Log.i(TAG, "No Movie Details " + movieId);
            return;
        }
        try {
            StringBuilder htmlReviews = new StringBuilder(getString(R.string.html_prefix));
            JSONObject movieJ = new JSONObject(movieDetails);
            JSONArray videos = movieJ.getJSONObject("videos").getJSONArray("results");
            int len = videos.length();
            Video[] theVideos = new Video[len];
            for (int i = 0; i < len; i++) {
                JSONObject videoJ = videos.getJSONObject(i);
                Log.i(TAG, videoJ.toString(2));
                Video v = new Video();
                v.setMovieId(movieId.toString());
                v.setName(videoJ.getString("name"));
                v.setUrlId(videoJ.getString("key"));
                v.setType(videoJ.getString("type"));
                Log.i(TAG, "adding video" + v);
                Log.i(TAG, "building string");
                theVideos[i] = v;
                // TODO could be a recyclerView
            }
            Video.setVideos(theVideos);
            ListAdapter adapter = new ImageTrailerAdapter(DetailActivity.this);
            final int adapterCount = adapter.getCount();
            for (int i = 0; i < adapterCount; i++) {
                View item = adapter.getView(i, null, null);
                item.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, v.getTag().toString());
                        Intent intent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse(YOUTUBE_LINK_URL + v.getTag()));
                        startActivity(intent);
                    }
                });
                mTrailerView.addView(item);
            }if (adapterCount == 0) {
                findViewById(R.id.tv_trailers).setVisibility(View.GONE);
                mTrailerView.setVisibility(View.GONE);
            } else {
                mTrailerView.setVisibility(View.VISIBLE);
                mTrailerView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i(TAG, v.toString());
                    }
                });
            }
            JSONArray reviews = movieJ.getJSONObject("reviews").getJSONArray("results");
            len = reviews.length();
            for (int i = 0; i < len; i++) {
                JSONObject reviewJ = reviews.getJSONObject(i);
                htmlReviews.append(getString(R.string.html_new_paragraph));
                if (i == 0) {
                    htmlReviews.append(getString(R.string.html_bold_ul_start))
                            .append(getString(R.string.reviews))
                            .append(getString(R.string.html_bold_ul_stop))
                            .append(getString(R.string.html_end_paragraph))
                            .append(getString(R.string.html_new_paragraph));
                }
                htmlReviews.append(getString(R.string.html_bold_start))
                        .append(reviewJ.getString("author"))
                        .append(getString(R.string.html_bold_stop))
                        .append(getString(R.string.html_br))
                        .append(reviewJ.getString("content"))
                        .append(getString(R.string.html_end_paragraph));

            }
            Log.i(TAG, htmlReviews.toString());
            mReviewView.loadData(htmlReviews.toString(),
                    "text/html; charset=utf-8", "utf-8");
        } catch (JSONException e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
