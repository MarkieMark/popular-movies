package com.halloit.mark.popularmovies;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.TextView;
//import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity.java";
//    private Toast mToast;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "MyTheMovieDBApiKeyV4: " + BuildConfig.THE_MOVIE_DB_API_KEY_V4);
        Log.i(TAG, "MyTheMovieDBApiKeyV3: " + BuildConfig.THE_MOVIE_DB_API_KEY_V3);
        GridView gridview = (GridView) findViewById(R.id.main_grid_view);
        gridview.setAdapter(new ImageMainAdapter(this));

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
//                if (mToast != null) {
//                    mToast.cancel();
//                }
//                mToast = Toast.makeText(MainActivity.this, "" + position,
//                        Toast.LENGTH_SHORT);
//                mToast.show();
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                // TODO put movie detail in the additional content
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
            // TODO change sort order as necessary
            // TODO implement logic for reload
        }
        if (parent.getItemAtPosition(pos).equals("Popular")) {
            Log.i(TAG, "selection: Popular");
            // TODO change sort order as necessary
            // TODO implement logic for reload
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
