package io.rolique.cameralibrary.screens.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.uiUtil.UiUtil;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import timber.log.Timber;

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
        void onImageClick(int position);

        void onRemoveClick(int position);
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
        mMediaContents.clear();
        mMediaContents.addAll(mediaContents);
        notifyDataSetChanged();
    }

    void updateAdapter(float parentHeight) {
        mParentHeight = parentHeight;
        notifyDataSetChanged();
    }

    void removeItem(int position) {
        mMediaContents.remove(position);
        notifyItemRemoved(position);
    }

    void addItem(MediaContent mediaContent) {
        mMediaContents.add(mediaContent);
        notifyItemInserted(mMediaContents.size() - 1);
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
            int viewHeight = height >= width ? maxDimensions : maxDimensions * height / width;
            int viewWidth = height >= width ? maxDimensions * width / height : maxDimensions;
            ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
            layoutParams.height = viewHeight;
            layoutParams.width = viewWidth;
            mImageView.setLayoutParams(layoutParams);
            setUpActionListeners();
            setImageWithRoundCorners(mImageView, mediaContent.getImage(), viewHeight, viewWidth);
            itemView.findViewById(R.id.image_view_play_video)
                    .setVisibility(mMediaContent.getMediaType().equals(MediaContent.CATEGORY_VIDEO) ?
                            View.VISIBLE : View.GONE);
        }

        private void setImageWithRoundCorners(ImageView imageView, String path, int viewHeight, int viewWidth) {
            File image = new File(path);
                int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
                Picasso picasso = new Picasso.Builder(imageView.getContext()).listener(new Picasso.Listener() {
                    @Override
                    public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                        exception.printStackTrace();
                    }
                }).build();
                picasso
                        .load(image)
                        .resize(viewWidth, viewHeight)
                        .transform(new RoundedCornersTransformation(cornerRadius, 0))
                        .placeholder(R.color.grey_300)
                        .into(imageView);
        }

        private void setUpActionListeners() {
            itemView.findViewById(R.id.layout_delete).setOnClickListener(mOnDeleteClickListener);
            itemView.findViewById(R.id.image_button_delete).setOnClickListener(mOnDeleteClickListener);
            mImageView.setOnClickListener(mOnImageClickListener);
        }

        View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnImagesClickListener.onRemoveClick(getAdapterPosition());
            }
        };

        View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnImagesClickListener.onImageClick(getAdapterPosition());
            }
        };
    }
}
