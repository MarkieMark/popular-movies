package com.halloit.mark.popularmovies;

import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{
    public static final String TAG = "MainActivity.java";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i("MyTheMovieDBApiKeyV4", BuildConfig.THE_MOVIE_DB_API_KEY_V4);
        Log.i("MyTheMovieDBApiKeyV3", BuildConfig.THE_MOVIE_DB_API_KEY_V3);
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
        if (((String) parent.getItemAtPosition(pos)).equals("Top Rated")) {
            Log.i(TAG, "selection: Top Rated");
            // TODO change sort order as necessary
            // TODO implement logic for reload
        }
        if (((String) parent.getItemAtPosition(pos)).equals("Popular")) {
            Log.i(TAG, "selection: Popular");
            // TODO change sort order as necessary
            // TODO implement logic for reload
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
