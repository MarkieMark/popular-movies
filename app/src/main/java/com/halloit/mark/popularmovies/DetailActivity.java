package com.halloit.mark.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity.java";
    private TextView mErrorView;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        mImageView = (ImageView) findViewById(R.id.dv_image);
        mProgressBar = (ProgressBar) findViewById(R.id.pb_loading_detail);
        mErrorView = (TextView) findViewById(R.id.tv_error_detail);
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            if (! Utils.isNetworkConnected(this)) {
                mErrorView.setText(R.string.no_network);
                mErrorView.setVisibility(View.VISIBLE);
                mImageView.setVisibility(View.INVISIBLE);
                mProgressBar.setVisibility(View.INVISIBLE);
                return;
            }
            // TODO check for actual connection
            String content = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.i(TAG, "intent content: " + content);
            mImageView.setMinimumHeight(750);
            Picasso.with(this).load(content).into(mImageView);
        }
    }
    // TODO add more detail
}
