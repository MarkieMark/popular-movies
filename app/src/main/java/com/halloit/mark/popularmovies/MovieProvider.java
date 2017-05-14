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

import com.halloit.mark.popularmovies.MovieContract.MovieDbHelper;
import com.halloit.mark.popularmovies.MovieContract.MovieEntry;


/**
 * Mark Benjamin 5/14/17.
 */

public class MovieProvider extends ContentProvider {

    static final int CODE_MOVIES = 100;
    static final int CODE_MOVIE_WITH_ID = 102;

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    private static final String[] PRIORITY_PROJECTION = {MovieEntry.COLUMN_POP_PRIORITY, MovieEntry.COLUMN_TR_PRIORITY};
    private static final int IND_PRIORITY_POP = 0;
    private static final int IND_PRIORITY_TR = 1;

    private static UriMatcher buildUriMatcher() {
        UriMatcher ret = new UriMatcher(UriMatcher.NO_MATCH);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, CODE_MOVIES);
        ret.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/#",
                CODE_MOVIE_WITH_ID);
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
                                c.moveToFirst();
                                if (isPop) {
                                    value.put(MovieEntry.COLUMN_TR_PRIORITY,
                                            c.getInt(IND_PRIORITY_TR));
                                } else {
                                    value.put(MovieEntry.COLUMN_POP_PRIORITY,
                                            c.getInt(IND_PRIORITY_POP));
                                }
                            }
                            long id = db.insert(MovieEntry.TABLE_NAME, null, value);
                            if (id != -1) {
                                rowsInserted++;
                            }
                            c.close();
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
            case CODE_MOVIES:
                ret = db.query(MovieEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
            case CODE_MOVIE_WITH_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                ret = db.query(MovieEntry.TABLE_NAME, projection, selection,
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
        switch (sUriMatcher.match(uri)) {
            case CODE_MOVIES:
                numRowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case CODE_MOVIE_WITH_ID:
                selection = MovieEntry.COLUMN_MOVIE_ID + " = ? ";
                selectionArgs = new String[]{uri.getLastPathSegment()};
                numRowsDeleted = db.delete(
                        MovieEntry.TABLE_NAME,
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
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("update() method not implemented");
    }
}
