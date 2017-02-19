package com.halloit.mark.popularmovies;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Author Mark
 */

class ImageMainAdapter extends BaseAdapter {
    private static final String TAG = "ImageMainAdapter.java";
    private final Context context;

    ImageMainAdapter(Context c) {
        context = c;
    }

    @Override
    public int getCount() {
        return Movie.getMovieList().length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            // not recycled, init some attributes
            imageView = new ImageView(context);
            imageView.setMinimumHeight(500);
            imageView.setMaxWidth(175);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(6, 6, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setTag(String.valueOf(position));

        Log.i(TAG, "picasso loading URL: " + Movie.getMovieList()[position].getImageFullPath());
        Picasso.with(context).load(Movie.getMovieList()[position].getImageFullPath()).into(imageView);
        return imageView;
    }
}
