package com.halloit.mark.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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
    private static final String[] COLUMN_PROJECTION = {_ID, COLUMN_VIDEO_KEY,
            COLUMN_VIDEO_TITLE, COLUMN_VIDEO_TYPE, COLUMN_VIDEO_IMAGE};
    private static final int IND_COLUMN_VIDEO_ID = 0;
    private static final int IND_COLUMN_VIDEO_KEY = 1;
    private static final int IND_COLUMN_VIDEO_TITLE = 2;
    private static final int IND_COLUMN_VIDEO_TYPE = 3;
    private static final int IND_COLUMN_VIDEO_IMAGE = 4;

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
        Log.i(TAG, "key " + c.getString(IND_COLUMN_VIDEO_KEY) + ", title " +
                c.getString(IND_COLUMN_VIDEO_TITLE) + ", type " + c.getString(IND_COLUMN_VIDEO_TYPE));
        imageLayout.setTag(c.getString(IND_COLUMN_VIDEO_KEY));
        String path = YOUTUBE_IMAGE_URL + c.getString(IND_COLUMN_VIDEO_KEY) + YOUTUBE_IMAGE_Q;
        Log.i(TAG, "picasso loading URL: " + path);
        Log.i(TAG, "type " + c.getString(IND_COLUMN_VIDEO_TYPE));
        new ImageRetrieval(imageView, path, c.getLong(IND_COLUMN_VIDEO_ID)).start();
        labelView.setText(c.getString(IND_COLUMN_VIDEO_TITLE));
        c.close();
        return imageLayout;
    }

    private class ImageRetrieval extends Thread {
        private ImageView view;
        private String path;
        private long videoId;

        ImageRetrieval(ImageView view, String path, long videoId) {
            this.view = view;
            this.path = path;
            this.videoId = videoId;
        }

        @Override
        public void run() {
            try {
                ContentResolver resolver = context.getContentResolver();
                String selection = VideoEntry._ID + " = ? ";
                String[] selectionArgs = {String.valueOf(videoId)};
                Cursor c = resolver.query(VideoEntry.CONTENT_URI,
                        COLUMN_PROJECTION, selection, selectionArgs, null);
                if (c == null) {
                    getNewImage();
                    return;
                }
                if (c.getCount() > 1) {
                    Log.i(TAG, "More than 1 entry for Video " + videoId);
                }
                c.moveToFirst();
                try {
                    final byte[] data = c.getBlob(IND_COLUMN_VIDEO_IMAGE);
                    if (data == null) {
                        c.close();
                        getNewImage();
                        return;
                    } else {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                view.setImageBitmap(BitmapFactory.decodeByteArray(
                                        data, 0, data.length));
                            }
                        });
                    }
                } catch (Exception E) {
                    E.printStackTrace();
                    Log.i(TAG, E.toString());
                    getNewImage();
                    return;
                }
                c.close();
            } catch (Exception E) {
                E.printStackTrace();
            }
        }

        private void getNewImage() {
            try {
                URL imageUrl = new URL(path);
                URLConnection uc = imageUrl.openConnection();

                InputStream is = uc.getInputStream();
                BufferedInputStream bis = new BufferedInputStream(is);

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int current;
                while ((current = bis.read()) != -1) {
                    buffer.write((byte) current);
                }
                final byte[] data = buffer.toByteArray();
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        view.setImageBitmap(BitmapFactory.decodeByteArray(
                                data, 0, data.length));
                    }
                });
                ContentValues values = new ContentValues();
                values.put(VideoEntry.COLUMN_VIDEO_IMAGE, data);
                String selection = VideoEntry._ID + " = ? ";
                String[] selectionArgs = {String.valueOf(videoId)};
                context.getContentResolver().update(VideoEntry.CONTENT_URI,
                        values, selection, selectionArgs);
            } catch (Exception E) {
                Log.i(TAG, E.toString());
                E.printStackTrace();
            }
        }
    }
}
