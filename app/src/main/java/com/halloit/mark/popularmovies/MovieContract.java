package com.halloit.mark.popularmovies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Mark Benjamin 5/12/17.
 */

class MovieContract {
    // retain all the database content in one file; at a mere 150 loc it's
    // quite manageable
    static final String CONTENT_AUTHORITY = "com.halloit.mark.popularmovies";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    static final String PATH_MOVIES = "movies";
    static final String PATH_VIDEOS = "videos";
    static final String PATH_REVIEWS = "reviews";

    // the movie table
    static class MovieEntry implements BaseColumns {
        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_MOVIES)
                .build();
        static final String TABLE_NAME = "movies";
        static final String COLUMN_TR_PRIORITY = "top_rated_priority";
        static final String COLUMN_POP_PRIORITY = "popular_priority";
        static final String COLUMN_MOVIE_ID = "movie_id";
        static final String COLUMN_TITLE = "title";
        static final String COLUMN_POSTER_PATH = "poster_path";
        static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        static final String COLUMN_ORIGINAL_TITLE = "original_title";
        static final String COLUMN_VOTE_AVERAGE = "vote_average";
        static final String COLUMN_RELEASE_DATE = "release_date";
        static final String COLUMN_ORIGINAL_LANGUAGE = "original_language";
        static final String COLUMN_OVERVIEW = "overview";
        static final String COLUMN_IMAGE_FULL_PATH = "image_full_path";
        static final String COLUMN_FAVORITE = "favorite";
        static final String COLUMN_POSTER = "poster";
        static Uri buildMovieUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }

    // the video table
    static class VideoEntry implements BaseColumns {
        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_VIDEOS)
                .build();
        static final String TABLE_NAME = "videos";
        static final String COLUMN_VIDEO_MOVIE_ID = "video_movie_id";
        static final String COLUMN_VIDEO_KEY = "video_key";
        static final String COLUMN_VIDEO_TYPE = "video_type";
        static final String COLUMN_VIDEO_TITLE = "video_title";
        static final String COLUMN_VIDEO_IMAGE = "video_image";
        static Uri buildVideoUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }

    // the review table
    static class ReviewEntry implements BaseColumns {
        static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_REVIEWS)
                .build();
        static final String TABLE_NAME = "reviews";
        static final String COLUMN_REVIEW_MOVIE_ID = "review_movie_id";
        static final String COLUMN_REVIEW_AUTHOR = "review_author";
        static final String COLUMN_REVIEW_CONTENT = "review_content";
        static Uri buildReviewUriWithId(long id) {
            return CONTENT_URI.buildUpon()
                    .appendPath(Long.toString(id))
                    .build();
        }
    }

    // utility intermediary class for db handling
    class MovieDbHelper extends SQLiteOpenHelper {
        private static final String TAG = "MovieDbHelper";
        private static final String DATABASE_NAME = "popular_movies.db";
        private static final int DATABASE_VERSION = 1;
        private final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " +
                MovieEntry.TABLE_NAME + " ( " +
                MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieEntry.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                MovieEntry.COLUMN_TR_PRIORITY + " INTEGER NULL, " +
                MovieEntry.COLUMN_POP_PRIORITY + " INTEGER NULL, " +
                MovieEntry.COLUMN_TITLE + " TEXT, " +
                MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                MovieEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
                MovieEntry.COLUMN_VOTE_AVERAGE + " REAL, " +
                MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MovieEntry.COLUMN_ORIGINAL_LANGUAGE + " TEXT, " +
                MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieEntry.COLUMN_IMAGE_FULL_PATH + " TEXT, " +
                MovieEntry.COLUMN_FAVORITE + " INTEGER DEFAULT 0, " +
                MovieEntry.COLUMN_POSTER + " BLOB DEFAULT NULL, " +
                "UNIQUE ( " + MovieEntry.COLUMN_MOVIE_ID + " ) ON CONFLICT REPLACE ) ;";
        private final String SQL_CREATE_VIDEO_TABLE = "CREATE TABLE " +
                VideoEntry.TABLE_NAME + " ( " +
                VideoEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                VideoEntry.COLUMN_VIDEO_MOVIE_ID + " INTEGER NOT NULL, " +
                VideoEntry.COLUMN_VIDEO_KEY + " TEXT NOT NULL, " +
                VideoEntry.COLUMN_VIDEO_TITLE + " TEXT, " +
                VideoEntry.COLUMN_VIDEO_TYPE + " TEXT, " +
                VideoEntry.COLUMN_VIDEO_IMAGE + " BLOB DEFAULT NULL ) ;";
        private final String SQL_CREATE_REVIEW_TABLE = "CREATE TABLE " +
                ReviewEntry.TABLE_NAME + " ( " +
                ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ReviewEntry.COLUMN_REVIEW_MOVIE_ID + " INTEGER NOT NULL, " +
                ReviewEntry.COLUMN_REVIEW_AUTHOR + " TEXT, " +
                ReviewEntry.COLUMN_REVIEW_CONTENT + " TEXT ) ;";

        MovieDbHelper(Context c) {
            super(c, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "OnCreate");
            try {
                db.execSQL(SQL_CREATE_MOVIE_TABLE);
                db.execSQL(SQL_CREATE_VIDEO_TABLE);
                db.execSQL(SQL_CREATE_REVIEW_TABLE);
            } catch (Exception E) {
                E.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Database Upgrade from " + oldVersion + " to " + newVersion);
            try {
                db.execSQL("DROP TABLE IF EXISTS " + MovieEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + VideoEntry.TABLE_NAME);
                db.execSQL("DROP TABLE IF EXISTS " + ReviewEntry.TABLE_NAME);
                onCreate(db);
            } catch (Exception E) {
                E.printStackTrace();
            }
        }
    }
}
