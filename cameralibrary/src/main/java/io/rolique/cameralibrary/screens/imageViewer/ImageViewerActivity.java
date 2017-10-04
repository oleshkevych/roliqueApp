package io.rolique.cameralibrary.screens.imageViewer;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.R2;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.widget.TouchImageView;

/**
 * Created by Volodymyr Oleshkevych on 5/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class ImageViewerActivity extends AppCompatActivity {

    private static final String EXTRA_IMAGE = "IMAGE_PATH";
    private static final String EXTRA_IMAGE_HEIGHT = "IMAGE_HEIGHT";
    private static final String EXTRA_IMAGE_WIDTH = "IMAGE_WIDTH";

    public static void start(Activity activity, ImageView imageView, MediaContent mediaContent) {
        Intent intent = new Intent(activity, ImageViewerActivity.class);
        intent.putExtra(EXTRA_IMAGE, mediaContent.getImage());
        intent.putExtra(EXTRA_IMAGE_HEIGHT, mediaContent.getHeight());
        intent.putExtra(EXTRA_IMAGE_WIDTH, mediaContent.getWidth());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity, imageView, activity.getString(R.string.shared_transition_image));
            activity.startActivity(intent, activityOptionsCompat.toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    @BindView(R2.id.touch_image_view) TouchImageView mImageView;

    int height;
    int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        ButterKnife.bind(ImageViewerActivity.this);
        ActivityCompat.postponeEnterTransition(ImageViewerActivity.this);
        setUpToolbar();
        File file = (File) getIntent().getSerializableExtra(EXTRA_IMAGE);
        height = getIntent().getIntExtra(EXTRA_IMAGE_HEIGHT, 0);
        width = getIntent().getIntExtra(EXTRA_IMAGE_WIDTH, 0);
        loadImage(file);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void loadImage(File path) {
        Picasso.with(mImageView.getContext())
                .load(path)
                .resize(width, height)
                .into(mImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        ActivityCompat.startPostponedEnterTransition(ImageViewerActivity.this);
                    }

                    @Override
                    public void onError() {
                        ActivityCompat.startPostponedEnterTransition(ImageViewerActivity.this);
                    }
                });
    }
}
