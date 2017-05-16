package com.halloit.mark.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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
import org.json.JSONObject;

import java.net.URL;

import static com.halloit.mark.popularmovies.BuildConfig.*;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;
import com.halloit.mark.popularmovies.MovieContract.VideoEntry;
import com.halloit.mark.popularmovies.MovieContract.ReviewEntry;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Utils.Boolean1String> {
    private static final String TAG = "DetailActivity.java";
    private LinearLayout mTrailerView;
    private WebView mReviewView;
    private Long movieId;
    private CheckBox mFavoriteButton;
    private TextView mErrorView;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayoutMain;
    private LinearLayout mReviewHeaderLayout;
    private ImageView mReviewHeaderImageView;

    private static final String[] COLUMN_MOVIE_PROJECTION = {MovieEntry._ID, MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IMAGE_FULL_PATH, MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE, MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW, MovieEntry.COLUMN_FAVORITE, MovieEntry.COLUMN_POSTER};
    private static final String[] COLUMN_REVIEW_PROJECTION = {ReviewEntry._ID,
            ReviewEntry.COLUMN_REVIEW_MOVIE_ID, ReviewEntry.COLUMN_REVIEW_AUTHOR,
            ReviewEntry.COLUMN_REVIEW_CONTENT};
//    private static final int IND_COLUMN__ID = 0;
//    private static final int IND_COLUMN_MOVIE_ID = 1;
    private static final int IND_COLUMN_IMAGE_FULL_PATH = 2;
    private static final int IND_COLUMN_TITLE = 3;
    private static final int IND_COLUMN_RELEASE_DATE = 4;
    private static final int IND_COLUMN_VOTE_AVERAGE = 5;
    private static final int IND_COLUMN_OVERVIEW = 6;
    private static final int IND_COLUMN_FAVORITE = 7;
    private static final int IND_COLUMN_POSTER = 8;

//    private static final int IND_REVIEW_COLUMN__ID = 0;
//    private static final int IND_REVIEW_COLUMN_MOVIE_ID = 1;
    private static final int IND_REVIEW_COLUMN_AUTHOR = 2;
    private static final int IND_REVIEW_COLUMN_CONTENT = 3;

    private static final String KEY_MOVIE_ID = "Movie_ID";
    private static final int MOVIE_DB_DETAIL_VIDEO_LOADER = 711;
    private static final int MOVIE_DB_DETAIL_REVIEW_LOADER = 713;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Immediate load of most of the data as it should already be present
        setContentView(R.layout.activity_detail);
        ImageView mImageView = (ImageView) findViewById(R.id.dv_image);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_detail);
        mLinearLayoutMain = (LinearLayout) findViewById(R.id.ll_detail_main);
        mErrorView = (TextView) findViewById(R.id.tv_error_detail);
        TextView mTitleView = (TextView) findViewById(R.id.tv_detail_title);
        TextView mDateView = (TextView) findViewById(R.id.tv_detail_date);
        TextView mScoreView = (TextView) findViewById(R.id.tv_detail_score);
        WebView mDescriptionView = (WebView) findViewById(R.id.tv_detail_description);
        mFavoriteButton = (CheckBox) findViewById(R.id.ib_favorite);
        mReviewView = (WebView) findViewById(R.id.wv_reviews);
        mTrailerView = (LinearLayout) findViewById(R.id.ll_trailer);
        mReviewHeaderLayout = (LinearLayout) findViewById(R.id.ll_reviews);
        mReviewHeaderImageView = (ImageView) findViewById(R.id.iv_review_toggle);
        Intent intent = getIntent();
        getSupportLoaderManager().initLoader(MOVIE_DB_DETAIL_VIDEO_LOADER, null, this);
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            String content = intent.getStringExtra(Intent.EXTRA_TEXT);
            movieId = Long.valueOf(content);
            Log.i(TAG, "intent content: " + content);
            Log.i(TAG, "movie details: " + movieId);
            mImageView.setMinimumHeight((int) getResources().getDimension(
                    R.dimen.main_image_min_height));
            mImageView.setMinimumWidth((int) getResources().getDimension(
                    R.dimen.detail_image_min_width));
            Cursor c = getContentResolver().query(MovieEntry.buildMovieUriWithId(movieId),
                    COLUMN_MOVIE_PROJECTION, null, null, null);
            if (c == null) {
                // can't even retrieve image path
                noData();
                return;
            }
            if (c.getCount() < 1) {
                c.close();
                noData();
                return;
            }
            c.moveToFirst();
            try {
                // initially, the image data should already be in the database
                byte[] data = c.getBlob(IND_COLUMN_POSTER);
                if (data != null) {
                    mImageView.setImageBitmap(BitmapFactory.decodeByteArray(
                            data, 0, data.length));
                } else {
                    // just in case
                    Picasso.with(this).load(c.getString(IND_COLUMN_IMAGE_FULL_PATH)).into(mImageView);
                }
            } catch (Exception E) {
                // absent data may throw some error
                E.printStackTrace();
                Picasso.with(this).load(c.getString(IND_COLUMN_IMAGE_FULL_PATH)).into(mImageView);
            }
            // retrieve the video thumbnails / youtube links
            Bundle bundle = new Bundle();
            bundle.putLong(KEY_MOVIE_ID, movieId);
            LoaderManager manager = getSupportLoaderManager();
            Loader<String> videoLoader = manager.getLoader(MOVIE_DB_DETAIL_VIDEO_LOADER);
            if (videoLoader == null) {
                manager.initLoader(MOVIE_DB_DETAIL_VIDEO_LOADER, bundle, this);
            } else {
                manager.restartLoader(MOVIE_DB_DETAIL_VIDEO_LOADER, bundle, this);
            }
            // retrieve the reviews
            Loader<String> reviewLoader = manager.getLoader(MOVIE_DB_DETAIL_REVIEW_LOADER);
            if (reviewLoader == null) {
                manager.initLoader(MOVIE_DB_DETAIL_REVIEW_LOADER, bundle, this);
            } else {
                manager.restartLoader(MOVIE_DB_DETAIL_REVIEW_LOADER, bundle, this);
            }
            // while that is happening, update the UI initially
            mTitleView.setText(c.getString(IND_COLUMN_TITLE));
            mDateView.setText(c.getString(IND_COLUMN_RELEASE_DATE).substring(0,4));
            mScoreView.setText(getString(R.string.vote_score, c.getFloat(IND_COLUMN_VOTE_AVERAGE)));
            mDescriptionView.loadData(getString(R.string.html_prefix) +
                    c.getString(IND_COLUMN_OVERVIEW) + getString(R.string.html_suffix),
                    "text/html; charset=utf-8", "utf-8");
            mFavoriteButton.setChecked(c.getInt(IND_COLUMN_FAVORITE) > 0);
            mReviewHeaderLayout.setVisibility(View.INVISIBLE);
            mReviewView.setVisibility(View.GONE);
            c.close();
        }
    }

    public void toggleFavorite(View view) {
        // handle favorite clicking
        Log.i(TAG, "toggleFavorite()");
        boolean isFavorite = mFavoriteButton.isChecked();
        // update the database
        ContentValues value = new ContentValues();
        String[] selectionArgs = new String[]{movieId.toString()};
        value.put(MovieEntry.COLUMN_FAVORITE, isFavorite ? 1 : 0);
        int numberOfRows = getContentResolver().update(MovieEntry.buildMovieUriWithId(movieId),
                value, MovieEntry.COLUMN_MOVIE_ID + " = ? ", selectionArgs);
        if (numberOfRows < 1) {
            // 'undo' checkbox click
            mFavoriteButton.setChecked(!isFavorite);
            // show error message
            Toast toast = Toast.makeText(this, "Error" + (isFavorite ? "Adding to" : "Removing From")
                    + " Favorites", Toast.LENGTH_LONG);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if( v != null) v.setGravity(Gravity.CENTER);
            toast.show();
            return;
        }
        // get the movie title for a friendly message
        Cursor c = getContentResolver().query(MovieEntry.buildMovieUriWithId(movieId),
                COLUMN_MOVIE_PROJECTION, null, null, null);
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

    private void noData() {
        if (! Utils.isNetworkConnected(this)) {
            mErrorView.setText(R.string.no_network);
        } else {
            mErrorView.setText(R.string.no_data);
        }
        mErrorView.setVisibility(View.VISIBLE);
//        mImageView.setVisibility(View.INVISIBLE);
        mLinearLayoutMain.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<Utils.Boolean1String> onCreateLoader(int id, final Bundle args) {
        // multipurpose AsyncTaskLoader for Videos as well as Reviews
        switch(id) {
            case MOVIE_DB_DETAIL_REVIEW_LOADER:
                // similarly to the MainActivity, 'struct' type class for additional data
                // referring to the review/video distinction
                return new AsyncTaskLoader<Utils.Boolean1String>(this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Utils.Boolean1String loadInBackground() {
                        Utils.Boolean1String ret = new Utils().new Boolean1String(false);
                        if (!Utils.isInternetAvailable()) {
                            return ret;
                        }
                        movieId = args.getLong(KEY_MOVIE_ID);
                        Log.i(TAG, "movieId " + movieId);
                        try {
                            URL url = new URL(MOVIE_DETAIL_URL + movieId +
                                    URL_REVIEW_Q + THE_MOVIE_DB_API_KEY_V3);
                            Log.i(TAG, "url: " + url.toString());
                            ret.jsonString = Utils.getResponseFromHttpUrl(url);
                            Log.i(TAG, "review json: " + ret.jsonString);
                        } catch (Exception e) {
                            Log.i(TAG, e.toString());
                            e.printStackTrace();
                        }
                        return ret;
                    }
                };
            case MOVIE_DB_DETAIL_VIDEO_LOADER:
                return new AsyncTaskLoader<Utils.Boolean1String>(this) {
                    @Override
                    protected void onStartLoading() {
                        forceLoad();
                    }

                    @Override
                    public Utils.Boolean1String loadInBackground() {
                        Utils.Boolean1String ret = new Utils().new Boolean1String(true);
                        if (!Utils.isInternetAvailable()) {
                            return ret;
                        }
                        movieId = args.getLong(KEY_MOVIE_ID);
                        Log.i(TAG, "movieId " + movieId);
                        try {
                            URL url = new URL(MOVIE_DETAIL_URL + movieId +
                                    URL_VIDEO_Q + THE_MOVIE_DB_API_KEY_V3);
                            Log.i(TAG, "url: " + url.toString());
                            ret.jsonString = Utils.getResponseFromHttpUrl(url);
                            Log.i(TAG, "video json: " + ret.jsonString);
                        } catch (Exception e) {
                            Log.i(TAG, e.toString());
                            e.printStackTrace();
                        }
                        return ret;
                    }
                };
            default:
                throw new UnsupportedOperationException("unrecognized code " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Utils.Boolean1String> loader,
                               Utils.Boolean1String movieDetails) {
        int len;
        try {
            if ((movieDetails == null) || (movieDetails.jsonString == null)) {
                Log.i(TAG, "No Movie Details " + movieId);
            } else {
                // data retrieved; save it to the database
                JSONObject movieJ = new JSONObject(movieDetails.jsonString);
                if (movieDetails.isVideo) {
                    // video data saving
                    JSONArray videos = movieJ.getJSONArray("results");
                    len = videos.length();
                    if (len == 0) return;
                    ContentValues[] values = new ContentValues[len];
                    for (int i = 0; i < len; i++) {
                        JSONObject videoJ = videos.getJSONObject(i);
                        Log.i(TAG, videoJ.toString(2));
                        values[i] = new ContentValues();
                        values[i].put(VideoEntry.COLUMN_VIDEO_MOVIE_ID, movieId);
                        values[i].put(VideoEntry.COLUMN_VIDEO_TITLE, videoJ.getString("name"));
                        values[i].put(VideoEntry.COLUMN_VIDEO_KEY, videoJ.getString("key"));
                        values[i].put(VideoEntry.COLUMN_VIDEO_TYPE, videoJ.getString("type"));
                    }
                    Log.i(TAG, "inserting video data");
                    getContentResolver().bulkInsert(VideoEntry.CONTENT_URI, values);
                } else {
                    // review data saving
                    JSONArray reviews = movieJ.getJSONArray("results");
                    len = reviews.length();
                    if (len == 0) return;
                    ContentValues[] values = new ContentValues[len];
                    for (int i = 0; i < len; i++) {
                        JSONObject reviewJ = reviews.getJSONObject(i);
                        values[i] = new ContentValues();
                        values[i].put(ReviewEntry.COLUMN_REVIEW_MOVIE_ID, movieId);
                        values[i].put(ReviewEntry.COLUMN_REVIEW_AUTHOR, reviewJ.getString("author"));
                        values[i].put(ReviewEntry.COLUMN_REVIEW_CONTENT, reviewJ.getString("content"));
                    }
                    getContentResolver().bulkInsert(ReviewEntry.CONTENT_URI, values);
                }
            }
            if ((movieDetails == null) || (movieDetails.isVideo)) {
                // null would be worthwhile retrieving from database even so
                Log.i(TAG, "creating video adapter");
                // let the adapter handle the actual retrieval of images
                ListAdapter adapter = new ImageTrailerAdapter(DetailActivity.this, movieId);
                Log.i(TAG, "retrieving adapter count");
                final int adapterCount = adapter.getCount();
                for (int i = 0; i < adapterCount; i++) {
                    Log.i(TAG, "retrieving adapter view " + i);
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
                }
                if (adapterCount == 0) {
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
                Log.i(TAG, "videos retrieved");
            }
            if ((movieDetails == null) || (!movieDetails.isVideo)) {
                // even when null, a retrieval from the database should be worthwhile
                StringBuilder htmlReviews = new StringBuilder(getString(R.string.html_prefix));
                Cursor c = getContentResolver().query(ReviewEntry.buildReviewUriWithId(movieId),
                        COLUMN_REVIEW_PROJECTION, null, null, null);
                if (c == null) {
                    mReviewHeaderLayout.setVisibility(View.GONE);
                    return;
                }
                if (c.getCount() > 0) {
                    mReviewHeaderLayout.setVisibility(View.VISIBLE);
                } else {
                    mReviewHeaderLayout.setVisibility(View.GONE);
                    c.close();
                    return;
                }
                while(c.moveToNext()) {
                    htmlReviews.append(getString(R.string.html_new_paragraph))
                            .append(getString(R.string.html_bold_start))
                            .append(c.getString(IND_REVIEW_COLUMN_AUTHOR))
                            .append(getString(R.string.html_bold_stop))
                            .append(getString(R.string.html_br))
                            .append(c.getString(IND_REVIEW_COLUMN_CONTENT))
                            .append(getString(R.string.html_end_paragraph));

                }
                c.close();
                htmlReviews.append(getString(R.string.html_suffix));
                Log.i(TAG, htmlReviews.toString());
                mReviewView.loadData(htmlReviews.toString(),
                        "text/html; charset=utf-8", "utf-8");
            }
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void onLoaderReset(Loader<Utils.Boolean1String> loader) {
    }

    public void toggleReview(View view) {
        boolean hide = (mReviewView.getVisibility() == View.VISIBLE);
        if (hide) {
            mReviewView.setVisibility(View.GONE);
            mReviewHeaderImageView.setRotation(90);
        } else {
            mReviewView.setVisibility(View.VISIBLE);
            mReviewHeaderImageView.setRotation(180);
        }
    }
}
