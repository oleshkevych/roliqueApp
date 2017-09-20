package io.rolique.roliqueapp.screens.imageViewer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.glide.GlideApp;
import io.rolique.roliqueapp.widget.TouchImageView;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE_PATH = "IMAGE_PATH";

    public static void start(Activity activity, ImageView imageView, Media media) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE_PATH, media.getImageUrl());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, imageView, activity.getString(R.string.shared_transition_image));
            activity.startActivity(intent, activityOptionsCompat.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @BindView(R.id.touch_image_view) TouchImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(ImageViewerActivity.this);
        ActivityCompat.postponeEnterTransition(ImageViewerActivity.this);
        String path = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        loadImage(path);
    }

    private void loadImage(String path) {
        GlideApp.with(imageView.getContext())
                .load(path)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Timber.e(e);
                        ActivityCompat.startPostponedEnterTransition(ImageViewerActivity.this);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ActivityCompat.startPostponedEnterTransition(ImageViewerActivity.this);
                        return false;
                    }
                })
                .into(imageView);
    }
}
