package io.rolique.cameralibrary.screens.imageViewer;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.widget.TouchImageView;

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
        mTouchImageView.setOnDragFinishedListener(new TouchImageView.OnDragFinishedListener() {
            @Override
            public void onDragFinished(boolean isFinished) {
                if (mToggleSwipeListener != null)
                    mToggleSwipeListener.onToggleSwipe(isFinished);
            }
        });
        loadImage(mTouchImageView);
    }

    private void loadImage(ImageView imageView) {
        Picasso.with(imageView.getContext())
                .load(mMediaContent.getImage())
                .resize(mMediaContent.getWidth(), mMediaContent.getHeight())
                .into(imageView);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser && mTouchImageView != null)
            mTouchImageView.removeZoom();
    }
}
