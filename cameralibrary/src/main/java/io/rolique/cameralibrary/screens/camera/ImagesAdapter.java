package io.rolique.cameralibrary.screens.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import io.rolique.cameralibrary.R;
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

        ImageView mImageView;
        MediaContent mMediaContent;

        ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_view);
        }

        void bindImage(MediaContent mediaContent) {
            mMediaContent = mediaContent;
            int height = mediaContent.getHeight();
            int width = mediaContent.getWidth();
            int maxDimensions = (int) Math.max(mMinHeight, mParentHeight);
            mImageView.getLayoutParams().height = height >= width ? maxDimensions : maxDimensions * height / width;
            mImageView.getLayoutParams().width = height >= width ? maxDimensions * width / height : maxDimensions;
            UiUtil.setImageWithRoundCorners(mImageView, mediaContent.getImage());
            setUpActionListeners();
        }

        private void setUpActionListeners() {
            itemView.findViewById(R.id.layout_delete).setOnClickListener(mOnDeleteClickListener);
            itemView.findViewById(R.id.image_button_delete).setOnClickListener(mOnDeleteClickListener);
            mImageView.setOnClickListener(mOnImageClickListener);
        }


        View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnImagesClickListener.onRemoveClick(mMediaContent);
            }
        };

        View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnImagesClickListener.onImageClick(mImageView, mMediaContent);
            }
        };
    }
}
