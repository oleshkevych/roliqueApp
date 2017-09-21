package io.rolique.cameralibrary.screens.camera;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.R2;
import io.rolique.cameralibrary.data.model.Media;
import io.rolique.cameralibrary.uiUtil.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 5/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private LayoutInflater mInflater;
    private List<Media> mMedias;
    private float mParentHeight;
    private final int mMinHeight;

    interface OnImagesClickListener {
        void onImageClick(ImageView imageView, Media media);

        void onRemoveClick(Media media);
    }

    private OnImagesClickListener mOnImagesClickListener;

    void setOnImagesClickListener(OnImagesClickListener onImagesClickListener) {
        mOnImagesClickListener = onImagesClickListener;
    }

    public void setMedias(List<Media> medias) {
        if (medias == null || medias.size() == 0) return;
        mMedias = medias;
        notifyDataSetChanged();
    }

    ImagesAdapter(Context context, int minHeight) {
        mInflater = LayoutInflater.from(context);
        mMedias = new ArrayList<>();
        mMinHeight = minHeight;
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
        holder.bindImage(mMedias.get(position));
    }

    @Override
    public int getItemCount() {
        return mMedias.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R2.id.image_button) ImageView mImageView;
        Media mMedia;

        ImageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(ImageViewHolder.this, itemView);
        }

        void bindImage(Media media) {
            mMedia = media;
            mImageView.getLayoutParams().height = (int) Math.max(mMinHeight, mParentHeight);
            mImageView.getLayoutParams().width = (int) Math.max(mMinHeight, mParentHeight) * 3 / 4;
            UiUtil.setImageWithRoundCorners(mImageView, media.getImage());
        }

        @OnClick(R2.id.image_button)
        void onImageClick() {
            mOnImagesClickListener.onImageClick(mImageView, mMedia);
        }

        @OnClick(R2.id.layout_delete)
        void onRemoveLayoutClick() {
            mOnImagesClickListener.onRemoveClick(mMedia);
        }

        @OnClick(R2.id.image_button_delete)
        void onRemoveButtonClick() {
            mOnImagesClickListener.onRemoveClick(mMedia);
        }
    }
}
