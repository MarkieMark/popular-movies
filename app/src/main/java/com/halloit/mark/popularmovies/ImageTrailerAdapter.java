package com.halloit.mark.popularmovies;

import android.content.Context;
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

/**
 * Author Mark
 */

class ImageTrailerAdapter extends BaseAdapter {
    private static final String TAG = "ImageTrailerAdapter";
    private final Context context;

    ImageTrailerAdapter(Context c) {
        context = c;
    }

    @Override
    public int getCount() {
        return Video.getVideos().length;
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
        // TODO could add title / type
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
        Video video = Video.getVideos()[position];
        imageLayout.setTag(String.valueOf(video.getUrlId()));
        String path = YOUTUBE_IMAGE_URL + video.getUrlId() + YOUTUBE_IMAGE_Q;
        Log.i(TAG, "picasso loading URL: " + path);
        Picasso.with(context).load(path).into(imageView);
        labelView.setText(video.getName());
        return imageLayout;
    }
}
