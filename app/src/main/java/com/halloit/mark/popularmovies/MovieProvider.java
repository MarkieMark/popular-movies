package com.halloit.mark.popularmovies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.halloit.mark.popularmovies.MovieContract.MovieDbHelper;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;
import com.halloit.mark.popularmovies.MovieContract.VideoEntry;
import com.halloit.mark.popularmovies.MovieContract.ReviewEntry;


/**
 * Mark Benjamin 5/14/17.
 */

public class MovieProvider extends ContentProvider {
    // the provider

    static final String TAG = "MovieProvider";

    static final int CODE_MOVIES = 100;
    static final int CODE_MOVIE_WITH_ID = 102;
    static final int CODE_VIDEOS = 200;
    static final int CODE_VIDEOS_WITH_ID = 201;
    static final int CODE_REVIEWS = 300;
    static final int CODE_REVIEWS_WITH_ID = 301;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    // for checking content rather than obliviously overwriting
    private static final String[] PRIORITY_PROJECTION = {MovieEntry.COLUMN_POP_PRIORITY,
            MovieEntry.COLUMN_TR_PRIORITY, MovieEntry.COLUMN_FAVORITE};
    private static final int IND_PRIORITY_POP = 0;
    private static final int IND_PRIORITY_TR = 1;
    private static final int IND_FAVORITE = 2;

    // 6 URIs, 2 per table, basic as well as with movie id
    private static UriMatcher buildUriMatcher() {
        UriMatcher ret = new UriMatcher(UriMatcher.NO_MATCH);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, CODE_MOVIES);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/#",
                CODE_MOVIE_WITH_ID);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEOS, CODE_VIDEOS);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEOS + "/#",
                CODE_VIDEOS_WITH_ID);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEWS, CODE_REVIEWS);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEWS + "/#",
                CODE_REVIEWS_WITH_ID);
        return ret;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieContract().new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int rowsInserted = 0;
        switch(sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                // check whether an entry would be overwritten, then in case of
                // need retrieve the necessary elements of the preceding data
                // before completing the individual insert
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        Long movieId = (Long) value.get(MovieEntry.COLUMN_MOVIE_ID);
                        String[] selectionArgs = {String.valueOf(movieId)};
                        boolean isPop = (value.getAsInteger(MovieEntry.COLUMN_POP_PRIORITY) > 0);
                        Cursor c = db.query(MovieEntry.TABLE_NAME,
                                PRIORITY_PROJECTION,
                                MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                                selectionArgs,
                                null,
                                null,
                                null);
                        if (c != null) {
                            if (c.getCount() > 0) {
                                if (c.getCount() > 1) { Log.i(TAG, "Large count! " +
                                        c.getCount() + " for Movie id " + movieId);}
                                c.moveToFirst();
                                // popular entry may already be present with a top rated score
                                if (isPop) {
                                    value.put(MovieEntry.COLUMN_TR_PRIORITY,
                                            c.getInt(IND_PRIORITY_TR));
                                    if (c.getInt(IND_PRIORITY_POP) !=
                                            value.getAsInteger(MovieEntry.COLUMN_POP_PRIORITY)) {
                                        Log.i(TAG, "Overwriting popular priority from " +
                                                c.getInt(IND_PRIORITY_POP) + " to " +
                                                value.getAsInteger(MovieEntry.COLUMN_POP_PRIORITY));
                                    }
                                } else {
                                    // top rated entry may already be present with a popular score
                                    value.put(MovieEntry.COLUMN_POP_PRIORITY,
                                            c.getInt(IND_PRIORITY_POP));
                                    if (c.getInt(IND_PRIORITY_TR) !=
                                            value.getAsInteger(MovieEntry.COLUMN_TR_PRIORITY)) {
                                        Log.i(TAG, "Overwriting popular priority from " +
                                                c.getInt(IND_PRIORITY_TR) + " to " +
                                                value.getAsInteger(MovieEntry.COLUMN_TR_PRIORITY));
                                    }
                                }
                                // retain favorite flag from before
                                value.put(MovieEntry.COLUMN_FAVORITE,
                                        c.getInt(IND_FAVORITE));
                                c.close();
                            }
                        }
                        long id = db.insert(MovieEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case CODE_VIDEOS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(VideoEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            case CODE_REVIEWS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long id = db.insert(ReviewEntry.TABLE_NAME, null, value);
                        if (id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                break;
            default:
                return super.bulkInsert(uri, values);
        }
        if (rowsInserted > 0) {
            Context c = getContext();
            if (c != null) c.getContentResolver().notifyChange(uri, null);
        }
        return rowsInserted;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        Cursor ret;
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        switch(sUriMatcher.match(uri)) {
            // the 'with id' requests are very similar, we just need to set the selection
            // parameters accordingly
            case CODE_MOVIE_WITH_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_MOVIES:
                ret = db.query(MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CODE_VIDEOS_WITH_ID:
                selection = VideoEntry.COLUMN_VIDEO_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_VIDEOS:
                ret = db.query(VideoEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CODE_REVIEWS_WITH_ID:
                selection = ReviewEntry.COLUMN_REVIEW_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_REVIEWS:
                ret = db.query(ReviewEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("unknown URI " + uri);
        }
        return ret;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("getType() method not implemented");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("insert() method not implemented");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int numRowsDeleted;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        if (null == selection) selection = "1";
        // similarly we simply adjust the selection parameters for 'with id' requests
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIE_WITH_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_MOVIES:
                numRowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_VIDEOS_WITH_ID:
                selection = VideoEntry.COLUMN_VIDEO_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_VIDEOS:
                numRowsDeleted = db.delete(
                        VideoEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_REVIEWS_WITH_ID:
                selection = ReviewEntry.COLUMN_REVIEW_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_REVIEWS:
                numRowsDeleted = db.delete(
                        ReviewEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown URI: " + uri);
        }
        if (numRowsDeleted != 0) {
            Context c = getContext();
            if (c != null) c.getContentResolver().notifyChange(uri, null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        int numberOfRowsAffected;
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // similarly we simply adjust the selection parameters for 'with id' requests
        switch(sUriMatcher.match(uri)) {
            case CODE_MOVIE_WITH_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_MOVIES:
                numberOfRowsAffected = db.update(MovieEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case CODE_VIDEOS_WITH_ID:
                selection = VideoEntry.COLUMN_VIDEO_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_VIDEOS:
                numberOfRowsAffected = db.update(VideoEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            case CODE_REVIEWS_WITH_ID:
                selection = ReviewEntry.COLUMN_REVIEW_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
            case CODE_REVIEWS:
                numberOfRowsAffected = db.update(ReviewEntry.TABLE_NAME, values,
                        selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("update() method not implemented");
        }
        if (numberOfRowsAffected > 0) {
            Context c = getContext();
            if (c != null) c.getContentResolver().notifyChange(uri, null);
        }
        return numberOfRowsAffected;
    }
}
