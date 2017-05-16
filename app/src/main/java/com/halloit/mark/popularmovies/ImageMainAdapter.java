package com.halloit.mark.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.halloit.mark.popularmovies.MainActivity.DisplayType;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;


/**
 * Author Mark
 */

class ImageMainAdapter extends BaseAdapter {
    private static final String TAG = "ImageMainAdapter";
    private final Context context;
    private DisplayType displayType;
    private static final String[] COLUMN_PROJECTION = {MovieEntry.COLUMN_MOVIE_ID,
            MovieEntry.COLUMN_IMAGE_FULL_PATH, MovieEntry.COLUMN_POSTER
            /*, MovieEntry.COLUMN_POP_PRIORITY, MovieEntry.COLUMN_TR_PRIORITY*/};
    private static final int IND_COLUMN_MOVIE_ID = 0;
    private static final int IND_COLUMN_IMAGE_FULL_PATH = 1;
    private static final int IND_COLUMN_POSTER = 2;

    ImageMainAdapter(Context c, DisplayType type) {
        Log.i(TAG, "new ImageMainAdapter() " + type);
        context = c;
        displayType = type;
    }

    void setDisplayType(DisplayType type) {
        boolean change = (type == displayType);
        displayType = type;
        if (change) notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        String selectionCol = MovieEntry.COLUMN_POP_PRIORITY;
        Uri uri = MovieEntry.CONTENT_URI;
        if (displayType == DisplayType.FAVORITES) {
            selectionCol = MovieEntry.COLUMN_FAVORITE;
        } else if (displayType == DisplayType.TOP_RATED) {
            selectionCol = MovieEntry.COLUMN_TR_PRIORITY;
        }
        int ret = 0;
        Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                selectionCol + " > 0 ", null, null);
        if (c != null) {
            ret = c.getCount();
            Log.i(TAG, "getCount(); displayType = " + displayType + ", number of Entries " + ret);
/*            while (c.moveToNext()) Log.i(TAG, c.getString(IND_COLUMN_IMAGE_FULL_PATH) + " " +
                    c.getLong(IND_COLUMN_MOVIE_ID) + " " + c.getInt(2) + " " + c.getInt(3));
*/            c.close();
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
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AdjustableImageView imageView = null;
        try {
            if (convertView == null) {
                // create new ImageView
                imageView = new AdjustableImageView(context);
                imageView.setMinimumHeight((int) context.getResources().getDimension(
                        R.dimen.main_image_min_height));
                imageView.setMaxWidth((int) context.getResources().getDimension(
                        R.dimen.main_image_max_width));
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setPadding((int) context.getResources().getDimension(
                        R.dimen.main_image_horizontal_padding),
                        (int) context.getResources().getDimension(
                                R.dimen.main_image_horizontal_padding),
                        (int) context.getResources().getDimension(
                                R.dimen.main_image_vertical_padding),
                        (int) context.getResources().getDimension(
                                R.dimen.main_image_vertical_padding));
                imageView.setAdjustViewBounds(true);
            } else {
                imageView = (AdjustableImageView) convertView;
            }
            Uri uri = MovieEntry.CONTENT_URI;
            String path = "no path";
            Long movieId = -1L;
            if (displayType == DisplayType.FAVORITES) {
                // should be immediate retrieval of data from database
                Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                        MovieEntry.COLUMN_FAVORITE + " = 1 ", null, MovieEntry._ID);
                if (c != null) {
                    c.moveToPosition(position);
                    path = c.getString(IND_COLUMN_IMAGE_FULL_PATH);
                    movieId = c.getLong(IND_COLUMN_MOVIE_ID);
                    c.close();
                }
            } else {
                // should be quick retrieval of data from database
                String[] selectionArgs = {String.valueOf(position + 1)};
                String selectionCol = MovieEntry.COLUMN_POP_PRIORITY;
                if (displayType == DisplayType.TOP_RATED) {
                    selectionCol = MovieEntry.COLUMN_TR_PRIORITY;
                }
                Cursor c = context.getContentResolver().query(uri, COLUMN_PROJECTION,
                        selectionCol + " = ? ", selectionArgs, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        c.moveToFirst();
                        path = c.getString(IND_COLUMN_IMAGE_FULL_PATH);
                        movieId = c.getLong(IND_COLUMN_MOVIE_ID);
                    } else {
                        Log.i(TAG, "0 count from " + selectionCol + " = " + selectionArgs[0]);
                    }
                    c.close();
                } else {
                    Log.i(TAG, "null from " + selectionCol + " = " + selectionArgs[0]);
                }
            }
            imageView.setTag(String.valueOf(movieId));
            Log.i(TAG, "loading Image; URL " + path);
            // defer loading of actual image in a side thread
            new ImageRetrieval(imageView, path, movieId).start();
        } catch (Exception E) {
            E.printStackTrace();
        }
        return imageView;
    }
    private class ImageRetrieval extends Thread {
        private ImageView view;
        private String path;
        private long movieId;

        ImageRetrieval(ImageView view, String path, long movieId) {
            this.view = view;
            this.path = path;
            this.movieId = movieId;
        }

        @Override
        public void run() {
            try {
                // initially try to retrieve image content from database
                ContentResolver resolver = context.getContentResolver();
                Cursor c = resolver.query(MovieEntry.buildMovieUriWithId(movieId),
                        COLUMN_PROJECTION, null, null, null);
                if (c == null) {
                    getNewImage();
                    return;
                }
                if (c.getCount() > 1) {
                    Log.i(TAG, "More than 1 entry for Movie " + movieId);
                }
                c.moveToFirst();
                try {
                    final byte[] data = c.getBlob(IND_COLUMN_POSTER);
                    if (data == null) {
                        c.close();
                        getNewImage();
                        return;
                    } else {
                        // need to update view from the main UI thread
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
            // in case of need to load image from the internet
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
                // enqueue UI update as a priority
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        view.setImageBitmap(BitmapFactory.decodeByteArray(
                                data, 0, data.length));
                    }
                });
                // update database while that is queueing
                ContentValues values = new ContentValues();
                values.put(MovieEntry.COLUMN_POSTER, data);
                context.getContentResolver().update(MovieEntry.buildMovieUriWithId(movieId),
                        values, null, null);
            } catch (Exception E) {
                Log.i(TAG, E.toString());
                E.printStackTrace();
            }
        }
    }
}
