package io.rolique.cameralibrary.screens.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.R2;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.uiUtil.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 5/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private LayoutInflater mInflater;
    private List<MediaContent> mMediaContents;
    private float mParentHeight;
    private final int mMinHeight;
    private int mRotateFrom;
    private int mRotateTo;
    private long mTimeOfTheLastAnimation;

    interface OnImagesClickListener {
        void onImageClick(ImageView imageView, MediaContent mediaContent);

        void onRemoveClick(MediaContent mediaContent);
    }

    private OnImagesClickListener mOnImagesClickListener;

    ImagesAdapter(Context context, int minHeight) {
        mInflater = LayoutInflater.from(context);
        mMediaContents = new ArrayList<>();
        mMinHeight = minHeight;
    }

    void setOnImagesClickListener(OnImagesClickListener onImagesClickListener) {
        mOnImagesClickListener = onImagesClickListener;
    }

    void setMediaContents(List<MediaContent> mediaContents) {
        if (mediaContents == null || mediaContents.size() == 0) return;
        mMediaContents = mediaContents;
        notifyDataSetChanged();
    }

    void updateAdapter(float parentHeight) {
        mParentHeight = parentHeight;
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(mInflater.inflate(R.layout.item_camera_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.bindImage(mMediaContents.get(position));
    }

    @Override
    public int getItemCount() {
        return mMediaContents.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.image_view) ImageView mImageView;
        MediaContent mMediaContent;

        ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(ImageViewHolder.this, itemView);
        }

        void bindImage(MediaContent mediaContent) {
            mMediaContent = mediaContent;
            int height = mediaContent.getHeight();
            int width = mediaContent.getWidth();
            int maxDimensions = (int) Math.max(mMinHeight, mParentHeight);
            mImageView.getLayoutParams().height = height >= width ? maxDimensions : maxDimensions * height / width;
            mImageView.getLayoutParams().width = height >= width ? maxDimensions * width / height : maxDimensions;
            UiUtil.setImageWithRoundCorners(mImageView, mediaContent.getImage());
        }

        @OnClick(R2.id.image_view)
        void onImageClick() {
            mOnImagesClickListener.onImageClick(mImageView, mMediaContent);
        }

        @OnClick(R2.id.layout_delete)
        void onRemoveLayoutClick() {
            mOnImagesClickListener.onRemoveClick(mMediaContent);
        }

        @OnClick(R2.id.image_button_delete)
        void onRemoveButtonClick() {
            mOnImagesClickListener.onRemoveClick(mMediaContent);
        }
    }
}
