package com.halloit.mark.popularmovies;

import android.content.Intent;
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

// TODO spare imageview for spare image
    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
            Movie movie = Movie.getMovieList()[Integer.valueOf(content)];
            Log.i(TAG, "intent content: " + content);
            Log.i(TAG, "movie details: " + movie);
            mImageView.setMinimumHeight(500);
            mImageView.setMinimumWidth(325);
            Picasso.with(this).load(movie.getImageFullPath()).into(mImageView);
            mTitleView.setText(movie.getTitle());
            mDateView.setText(movie.getReleaseDate().substring(0,4));
            mScoreView.setText(String.valueOf(movie.getVoteAverage()) + "/10");
            mDescriptionView.loadData(getString(R.string.html_prefix) +
                    movie.getOverview() + getString(R.string.html_suffix),
                    "text/html; charset=utf-8", "utf-8");
        }
    }
}
