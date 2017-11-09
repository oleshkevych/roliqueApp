package io.rolique.roliqueapp.screens.imageViewer;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.glide.GlideApp;
import io.rolique.roliqueapp.widget.CustomViewPager;
import io.rolique.roliqueapp.widget.TouchImageView;

/**
 * Created by Volodymyr Oleshkevych on 10/13/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ImageFragment extends Fragment {

    private static String ARGUMENT_MEDIA_CONTENT = "MEDIA_CONTENT";

    public static ImageFragment newInstance(Media mediaContent) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_MEDIA_CONTENT, mediaContent);
        fragment.setArguments(args);
        return fragment;
    }

    private Media mMedia;
    CustomViewPager mCustomViewPager;
    private TouchImageView mTouchImageView;

    public TouchImageView getTouchImageView() {
        return mTouchImageView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMedia = getArguments().getParcelable(ARGUMENT_MEDIA_CONTENT);
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTouchImageView = view.findViewById(R.id.touch_image_view);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        mCustomViewPager = getViewPager(view.getParent());
        mTouchImageView.setOnDragFinishedListener(new TouchImageView.OnDragFinishedListener() {
            @Override
            public void onDragFinished(boolean isFinished) {
                if (mCustomViewPager != null)
                    mCustomViewPager.setScroll(isFinished);
            }
        });
        final LinearLayout errorLayout = view.findViewById(R.id.error_layout);
        errorLayout.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                loadImage(progressBar, errorLayout);
            }
        });
        loadImage(progressBar, errorLayout);
    }

    private CustomViewPager getViewPager(ViewParent viewParent) {
        if (viewParent instanceof View && viewParent instanceof CustomViewPager)
            return (CustomViewPager) viewParent;
        else {
            ViewParent parent = viewParent.getParent();
            return getViewPager(parent);
        }
    }

    private void loadImage(final ProgressBar progressBar, final LinearLayout errorLayout) {
        int maxDim = Math.max(mMedia.getHeight(), mMedia.getWidth());
        final int MAX_SIZE = 1500;
        GlideApp.with(mTouchImageView)
                .load(mMedia.getImageUrl())
                .override(MAX_SIZE * mMedia.getWidth() / maxDim, MAX_SIZE * mMedia.getHeight() / maxDim)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        errorLayout.setVisibility(View.VISIBLE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(mTouchImageView);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mCustomViewPager != null)
                mCustomViewPager.setScroll(true);
        } else if (mTouchImageView != null) {
            mTouchImageView.removeZoom();
        }
    }
}
