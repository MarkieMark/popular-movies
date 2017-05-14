package com.halloit.mark.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.halloit.mark.popularmovies.MovieContract.MovieEntry;

import com.squareup.picasso.Picasso;

/**
 * Author Mark
 */

class ImageMainAdapter extends BaseAdapter {
    private static final String TAG = "ImageMainAdapter.java";
    private final Context context;
    private final boolean isPop, isFav;
    private static final String[] COLUMN_PROJECTION = {MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IMAGE_FULL_PATH};
    private static final int IND_COLUMN_MOVIE_ID = 0;
    private static final int IND_COLUMN_IMAGE_FULL_PATH = 1;

    ImageMainAdapter(Context c, boolean isPop, boolean isFav) {
        context = c;
        this.isPop = isPop;
        this.isFav = isFav;
    }

    @Override
    public int getCount() {
        String selectionCol = MovieEntry.COLUMN_POP_PRIORITY;
        Uri uri = MovieEntry.CONTENT_URI;
        if (isFav) {
            selectionCol = MovieEntry.COLUMN_FAVORITE;
        } else if (!isPop) {
            selectionCol = MovieEntry.COLUMN_TR_PRIORITY;
        }
        int ret = 0;
        Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                selectionCol + " > 0 ", null, null);
        if (c != null) {
            ret = c.getCount();
            Log.i(TAG, "getCount(); isPop = " + isPop + ", isFav = " + isFav +
                    ", number of Entries " + ret);
            c.close();
        }
        return ret;
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
        Uri uri = MovieEntry.CONTENT_URI;
        String path = "no path";
        Long movieId = -1L;
        if (isFav) {
            Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                    MovieEntry.COLUMN_FAVORITE + " = 1 ", null, MovieEntry._ID);
            if (c != null) {
                c.moveToPosition(position);
                path = c.getString(IND_COLUMN_IMAGE_FULL_PATH);
                movieId = c.getLong(IND_COLUMN_MOVIE_ID);
                c.close();
            }
        } else {
            String[] selectionArgs = {String.valueOf(position + 1)};
            String selectionCol = MovieEntry.COLUMN_POP_PRIORITY;
            if (!isPop) selectionCol = MovieEntry.COLUMN_TR_PRIORITY;
            Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                    selectionCol + " = ? ", selectionArgs, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    path = c.getString(IND_COLUMN_IMAGE_FULL_PATH);
                    movieId = c.getLong(IND_COLUMN_MOVIE_ID);
                }
                c.close();
            }
        }
        imageView.setTag(String.valueOf(movieId));
        Log.i(TAG, "picasso loading URL: " + path);
        Picasso.with(context).load(path).into(imageView);
        return imageView;
    }
}
