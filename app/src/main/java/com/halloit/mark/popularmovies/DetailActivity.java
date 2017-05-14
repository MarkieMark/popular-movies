package com.halloit.mark.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static com.halloit.mark.popularmovies.BuildConfig.*;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;

// TODO favorites ContentProvider SQL database
// TODO refactor AsyncTasks to TaskLoader.Callbacks with database backing too

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity.java";
    private TextView mErrorView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;
    private LinearLayout mLinearLayoutMain;
    private TextView mTitleView;
    private TextView mDateView;
    private TextView mScoreView;
    private WebView mDescriptionView;

    private static final String[] COLUMN_PROJECTION = {MovieEntry._ID, MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IMAGE_FULL_PATH, MovieEntry.COLUMN_TITLE,
            MovieEntry.COLUMN_RELEASE_DATE, MovieEntry.COLUMN_VOTE_AVERAGE,
            MovieEntry.COLUMN_OVERVIEW};
    private static final int IND_COLUMN__ID = 0;
    private static final int IND_COLUMN_MOVIE_ID = 1;
    private static final int IND_COLUMN_IMAGE_FULL_PATH = 2;
    private static final int IND_COLUMN_TITLE = 3;
    private static final int IND_COLUMN_RELEASE_DATE = 4;
    private static final int IND_COLUMN_VOTE_AVERAGE = 5;
    private static final int IND_COLUMN_OVERVIEW = 6;

// TODO spare imageview for spare image
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO add favorite button

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mImageView = (ImageView) findViewById(R.id.dv_image);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_detail);
        mErrorView = (TextView) findViewById(R.id.tv_error_detail);
        mLinearLayoutMain = (LinearLayout) findViewById(R.id.ll_detail_main);
        mTitleView = (TextView) findViewById(R.id.tv_detail_title);
        mDateView = (TextView) findViewById(R.id.tv_detail_date);
        mScoreView = (TextView) findViewById(R.id.tv_detail_score);
        mDescriptionView = (WebView) findViewById(R.id.tv_detail_description);
        Intent intent = getIntent();
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
            Long movieId = Long.valueOf(content);
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
            new FetchMovieDetailsTask().execute(movieId);
            mTitleView.setText(c.getString(IND_COLUMN_TITLE));
            mDateView.setText(c.getString(IND_COLUMN_RELEASE_DATE).substring(0,4));
            mScoreView.setText(String.valueOf(c.getString(IND_COLUMN_VOTE_AVERAGE)) + "/10");
            mDescriptionView.loadData(getString(R.string.html_prefix) +
                    c.getString(IND_COLUMN_OVERVIEW) + getString(R.string.html_suffix),
                    "text/html; charset=utf-8", "utf-8");
            c.close();
        }
    }

    private class FetchMovieDetailsTask extends AsyncTask<Long, Void, String> {
        Long movieId;
        @Override
        protected void onPreExecute() {
            Log.i(TAG, "preparing to retrieve");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Long... longIds) {
            if (! Utils.isInternetAvailable()) {
                return null;
            }
            movieId = longIds[0];
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

        @Override
        protected void onPostExecute(String movieDetails) {
            if (movieDetails == null) {
                Log.i(TAG, "No Movie Details " + movieId);
                return;
            }
            try {
                Cursor c = getContentResolver().query(MovieEntry.buildMovieUriWithId(movieId),
                        COLUMN_PROJECTION, null, null, null);
                if (c == null) return;
                if (c.getCount() < 1) {
                    c.close();
                    return;
                }
                c.moveToFirst();
                StringBuilder htmlString = new StringBuilder(getString(R.string.html_prefix) +
                        getString(R.string.html_new_paragraph) + c.getString(IND_COLUMN_OVERVIEW) +
                        getString(R.string.html_end_paragraph));
                JSONObject movieJ = new JSONObject(movieDetails);
                JSONArray videos = movieJ.getJSONObject("videos").getJSONArray("results");
                int len = videos.length();
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
                    // TODO refactor into a horizontal scrollview with recyclerView to hold images
                    // TODO should launch youtube androidly
                    if (i == 0) htmlString.append(getString(R.string.html_new_paragraph))
                            .append(getString(R.string.html_bold_ul_start))
                            .append(getString(R.string.trailers))
                            .append(getString(R.string.html_bold_ul_stop))
                            .append(getString(R.string.html_end_paragraph))
                            .append(getString(R.string.html_start_table));
                    if (i % 2 == 0) {
                        htmlString.append(getString(R.string.html_start_tr));
                    }
                    htmlString.append(getString(R.string.html_start_td))
                            .append(getString(R.string.html_start_anchor))
                            .append(YOUTUBE_LINK_URL)
                            .append(v.getUrlId())
                            .append(getString(R.string.html_complete_quote_tag))
                            .append(v.getName()).append(" - ").append(v.getType())
                            .append(getString(R.string.html_br))
                            .append(getString(R.string.html_start_img))
                            .append(YOUTUBE_IMAGE_URL)
                            .append(v.getUrlId())
                            .append(YOUTUBE_IMAGE_Q)
                            .append(getString(R.string.html_complete_quote_tag))
                            .append(getString(R.string.html_close_anchor))
                            .append(getString(R.string.html_end_td));
                    if (i % 2 == 1) {
                        htmlString.append(getString(R.string.html_end_tr))
                                .append(getString(R.string.html_start_tr))
                                .append(getString(R.string.html_start_td))
                                .append(getString(R.string.html_3_space))
                                .append(getString(R.string.html_end_td))
                                .append(getString(R.string.html_start_td))
                                .append(getString(R.string.html_3_space))
                                .append(getString(R.string.html_end_td))
                                .append(getString(R.string.html_end_tr));
                    }
                    Log.i(TAG, htmlString.toString());
                }
                if (len % 2 == 1) {
                    htmlString.append(getString(R.string.html_start_td))
                            .append(getString(R.string.html_end_td))
                            .append(getString(R.string.html_end_tr));
                }
                if (len > 0) {
                    htmlString.append(getString(R.string.html_end_table));
                }
                htmlString.append(getString(R.string.html_suffix));
                JSONArray reviews = movieJ.getJSONObject("reviews").getJSONArray("results");
                len = reviews.length();
                for (int i = 0; i < len; i++) {
                    JSONObject reviewJ = reviews.getJSONObject(i);
                    htmlString.append(getString(R.string.html_new_paragraph));
                    if (i == 0) {
                        htmlString.append(getString(R.string.html_bold_ul_start))
                                .append(getString(R.string.reviews))
                                .append(getString(R.string.html_bold_ul_stop))
                                .append(getString(R.string.html_end_paragraph))
                                .append(getString(R.string.html_new_paragraph));
                    }
                    htmlString.append(getString(R.string.html_bold_start))
                            .append(reviewJ.getString("author"))
                            .append(getString(R.string.html_bold_stop))
                            .append(getString(R.string.html_br))
                            .append(reviewJ.getString("content"))
                            .append(getString(R.string.html_end_paragraph));

                }
                Log.i(TAG, htmlString.toString());
                mDescriptionView.loadData(htmlString.toString(),
                        "text/html; charset=utf-8", "utf-8");
            } catch (JSONException e) {
                Log.i(TAG, e.toString());
                e.printStackTrace();
            }
        }
    }
}
