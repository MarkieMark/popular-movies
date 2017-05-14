package com.halloit.mark.popularmovies;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Mark Benjamin 5/12/17.
 */

class MovieSyncIntentService extends IntentService {
    MovieSyncIntentService() {
        super("MovieSyncIntentService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        new Thread(new Runnable() {
            public void run() {
                TopRatedMovieSyncTask.syncMovies(MovieSyncIntentService.this);
            }
        }).start();
        PopularMovieSyncTask.syncMovies(this);
    }

    private static class TopRatedMovieSyncTask {
        private static final String TAG = "topRatedMovieSyncTask";
        synchronized private static void syncMovies(Context c) {
            try {
                // TODO create static utility method to retrieve content
                ContentValues[] topRatedValues = null;
                ContentResolver resolver = c.getContentResolver();
                // if ((topRatedValues != null) && (topRatedValues.length != 0)) {
                // resolver.delete(MovieContract.topRatedMovieEntry.topRated_URI,
                // null, null);
                // resolver.bulkInsert(MovieContract.topRatedMovieEntry.topRated_URI,
                // topRatedValues);
                // }
            } catch (Exception E) {
                E.printStackTrace();
                Log.i(TAG, E.toString());
            }
        }
    }

    private static class PopularMovieSyncTask {
        private static final String TAG = "PopularMovieSyncTask";
        synchronized private static void syncMovies(Context c) {
            try {
                // TODO create static utility method to retrieve content
                ContentValues[] popularValues = null;
                ContentResolver resolver = c.getContentResolver();
                // if ((popularValues != null) && (popularValues.length != 0)) {
                // resolver.delete(MovieContract.PopularMovieEntry.POPULAR_URI,
                // null, null);
                // resolver.bulkInsert(MovieContract.PopularMovieEntry.POPULAR_URI,
                // popularValues);
                // }
            } catch (Exception E) {
                E.printStackTrace();
                Log.i(TAG, E.toString());
            }
        }
    }
}
