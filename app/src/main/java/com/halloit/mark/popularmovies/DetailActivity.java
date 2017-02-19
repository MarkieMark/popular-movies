package com.halloit.mark.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

public class DetailActivity extends AppCompatActivity {
    private static final String TAG = "DetailActivity.java";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent();
        if (intent.hasExtra(Intent.EXTRA_TEXT)) {
            String content = intent.getStringExtra(Intent.EXTRA_TEXT);
            Log.i(TAG, "intent content: " + content);
            Integer id = getResources().getIdentifier(
                    "sample_" + content,
                    "drawable",
                    getPackageName());
            ((ImageView) findViewById(R.id.dv_image)).setImageResource(id);
        }
    }
}
