package io.rolique.cameralibrary.screens.imageViewer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.widget.TouchImageView;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 10/13/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ImageFragment extends Fragment {

    private static String ARGUMENT_MEDIA_CONTENT = "MEDIA_CONTENT";

    public static ImageFragment newInstance(MediaContent mediaContent) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_MEDIA_CONTENT, mediaContent);
        fragment.setArguments(args);
        return fragment;
    }

    interface OnToggleSwipeListener {
        void onToggleSwipe(boolean isAllowed);
    }

    private MediaContent mMediaContent;
    private OnToggleSwipeListener mToggleSwipeListener;
    private TouchImageView mTouchImageView;

    public void setToggleSwipeListener(OnToggleSwipeListener toggleSwipeListener) {
        mToggleSwipeListener = toggleSwipeListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMediaContent = getArguments().getParcelable(ARGUMENT_MEDIA_CONTENT);
        return inflater.inflate(R.layout.fragment_image, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTouchImageView = view.findViewById(R.id.touch_image_view);
        final ProgressBar progressBar = view.findViewById(R.id.progress_bar);
        mTouchImageView.setOnDragFinishedListener(new TouchImageView.OnDragFinishedListener() {
            @Override
            public void onDragFinished(boolean isFinished) {
                if (mToggleSwipeListener != null)
                    mToggleSwipeListener.onToggleSwipe(isFinished);
            }
        });
        final LinearLayout errorLayout = view.findViewById(R.id.error_layout);
        errorLayout.findViewById(R.id.button_retry).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                loadImage(mTouchImageView, progressBar, errorLayout);
            }
        });
        loadImage(mTouchImageView, progressBar, errorLayout);
    }

    private void loadImage(final ImageView imageView, final ProgressBar progressBar, final LinearLayout errorLayout) {

        Picasso built = Picasso.with(getActivity());
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        if (mMediaContent.getImage().startsWith("http")) {
            built.load(mMediaContent.getImage())
                    .resize(mMediaContent.getWidth(), mMediaContent.getHeight())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            //Try again online if cache failed
                            Picasso.with(getActivity())
                                    .load(mMediaContent.getImage())
                                    .into(imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            progressBar.setVisibility(View.GONE);
                                        }

                                        @Override
                                        public void onError() {
                                            Timber.e("Picasso", "Could not fetch image");
                                            progressBar.setVisibility(View.GONE);
                                            errorLayout.setVisibility(View.VISIBLE);
                                        }
                                    });
                        }
                    });
        } else {
            built.load(new File(mMediaContent.getImage()))
                    .resize(mMediaContent.getWidth(), mMediaContent.getHeight())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            progressBar.setVisibility(View.GONE);
                        }

                        @Override
                        public void onError() {
                            Timber.e("Picasso", "Could not fetch image");
                            progressBar.setVisibility(View.GONE);
                            errorLayout.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (mToggleSwipeListener != null)
                mToggleSwipeListener.onToggleSwipe(true);
        } else if (mTouchImageView != null) {
            mTouchImageView.removeZoom();
        }
    }
}
