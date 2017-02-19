package com.halloit.mark.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.util.Scanner;

/**
 * Created by mark on 2/19/17.
 */

public class Utils {
    private static final String TAG = "Utils.java";

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        Log.i(TAG, "getResponseFromHttpUrl(): " + url.toString());
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        Log.i(TAG, "HttpURLConnection: " + urlConnection.toString());
        try {
            InputStream in = urlConnection.getInputStream();
            Log.i(TAG, "InputStream: " + in.toString());
            Scanner scanner = new Scanner(in);
            Log.i(TAG, "Scanner: " + scanner.toString());
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            Log.i(TAG, "scanning");
            if (hasInput) {
                Log.i(TAG, "input retrieved");
                return scanner.next();
            } else {
                Log.i(TAG, "no input");
                return null;
            }
        } finally {
            Log.i(TAG, "disconnecting");
            urlConnection.disconnect();
        }
    }

    public static boolean isNetworkConnected(Context c) {
        Log.i(TAG, "isNetworkConnected");
        ConnectivityManager m = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return m.getActiveNetworkInfo() != null;
    }

    public static boolean isInternetAvailable() {
        Log.i(TAG, "isInternetAvailable");
        try {
            InetAddress ipAddr = InetAddress.getByName("themoviedb.com");
            return !ipAddr.equals("");
        } catch (Exception e) {
            Log.i(TAG, e.toString());
            return false;
        }
    }
}
