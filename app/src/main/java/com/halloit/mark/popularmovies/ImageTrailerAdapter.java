package com.halloit.mark.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static com.halloit.mark.popularmovies.BuildConfig.*;
import static com.halloit.mark.popularmovies.MovieContract.VideoEntry;
import static com.halloit.mark.popularmovies.MovieContract.VideoEntry.*;

/**
 * Author Mark
 */

class ImageTrailerAdapter extends BaseAdapter {
    private static final String TAG = "ImageTrailerAdapter";
    private final Context context;
    private long movieId;
    private static final String[] COLUMN_PROJECTION = {COLUMN_VIDEO_KEY,
            COLUMN_VIDEO_TITLE, COLUMN_VIDEO_TYPE};
    private static final int ID_COLUMN_VIDEO_KEY = 0;
    private static final int ID_COLUMN_VIDEO_TITLE = 1;
    private static final int ID_COLUMN_VIDEO_TYPE = 2;

    ImageTrailerAdapter(Context c, long movieId) {
        context = c;
        this.movieId = movieId;
    }

    @Override
    public int getCount() {
        Cursor c = context.getContentResolver()
                .query(VideoEntry.buildVideoUriWithId(movieId),
                        null,
                        null,
                        null,
                        VideoEntry._ID);
        if (c == null) return 0;
        int ret = c.getCount();
        c.close();
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
        LinearLayout imageLayout;
        TextView labelView;
        if (convertView == null) {
            // not recycled, init some attributes
            imageLayout = new LinearLayout(context);
            imageView = new ImageView(context);
            labelView = new TextView(context);
            labelView.setGravity(Gravity.CENTER);
            imageView.setMinimumHeight(300);
            imageView.setMinimumWidth(450);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setPadding(3, 3, 4, 4);
            imageLayout.setOrientation(LinearLayout.VERTICAL);
            imageLayout.addView(labelView);
            imageLayout.addView(imageView);
        } else {
            imageLayout = (LinearLayout) convertView;
            labelView = (TextView) imageLayout.getChildAt(0);
            imageView = (ImageView) imageLayout.getChildAt(1);
        }
        Cursor c = context.getContentResolver()
                .query(VideoEntry.buildVideoUriWithId(movieId),
                        COLUMN_PROJECTION,
                        null,
                        null,
                        VideoEntry._ID);
        if ((c == null) || (c.getCount() < 1)) return imageLayout;
        Log.i(TAG, "position " + position);
        c.moveToPosition(position);
        Log.i(TAG, "key " + c.getString(ID_COLUMN_VIDEO_KEY) + ", title " +
                c.getString(ID_COLUMN_VIDEO_TITLE) + ", type " + c.getString(ID_COLUMN_VIDEO_TYPE));
        imageLayout.setTag(c.getString(ID_COLUMN_VIDEO_KEY));
        String path = YOUTUBE_IMAGE_URL + c.getString(ID_COLUMN_VIDEO_KEY) + YOUTUBE_IMAGE_Q;
        Log.i(TAG, "picasso loading URL: " + path);
        Log.i(TAG, "type " + c.getString(ID_COLUMN_VIDEO_TYPE));
        Picasso.with(context).load(path).into(imageView);
        labelView.setText(c.getString(ID_COLUMN_VIDEO_TITLE));
        c.close();
        return imageLayout;
    }
}
