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
    private String[] mPosterUrls;

    ImageMainAdapter(Context c, String[] imagePaths) {
        context = c;
        mPosterUrls = imagePaths;
    }

    @Override
    public int getCount() {
        return mPosterUrls.length;
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
        imageView.setTag(mPosterUrls[position]);

        Log.i(TAG, "picasso loading URL: " + mPosterUrls[position]);
        Picasso.with(context).load(mPosterUrls[position]).into(imageView);
        return imageView;
    }

    public void setMPosterUrls(String[] mPosterUrls) {
        this.mPosterUrls = mPosterUrls;
    }
}
