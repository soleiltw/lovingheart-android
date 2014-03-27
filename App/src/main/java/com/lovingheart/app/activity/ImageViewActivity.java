package com.lovingheart.app.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import com.lovingheart.app.R;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.polites.android.GestureImageView;

/**
 * Created by edward_chiang on 2014/3/28.
 */
public class ImageViewActivity extends ActionBarActivity {

    protected GestureImageView singleImageView;

    protected View singleImageViewLayout;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33000000")));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#55000000")));
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        setContentView(R.layout.activity_imageview_main);

        imageUrl = getIntent().getStringExtra("imageUrl");
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        singleImageView = (GestureImageView)findViewById(R.id.single_image_view);

        if (imageUrl!=null) {
            ImageLoader.getInstance().loadImage(imageUrl, new SimpleImageLoadingListener(){
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    singleImageView.setImageBitmap(loadedImage);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
