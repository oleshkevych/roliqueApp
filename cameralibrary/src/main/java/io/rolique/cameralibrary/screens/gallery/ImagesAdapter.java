package io.rolique.cameralibrary.screens.gallery;

import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Volodymyr Oleshkevych on 5/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {

    private LayoutInflater mInflater;
    private List<MediaContent> mMediaContents;
    private Point mDisplaySize;
    private List<String> mMediaContentsSelected;
    Picasso mPicasso;

    interface OnImagesClickListener {
        void onAddImageClick(int position);

        void onRemoveImageClick(int position);
    }

    private OnImagesClickListener mOnImagesClickListener;

    ImagesAdapter(Context context,
                  OnImagesClickListener onImagesClickListener,
                  Point displaySize) {
        mInflater = LayoutInflater.from(context);
        mMediaContents = new ArrayList<>();
        mMediaContentsSelected = new ArrayList<>();
        mOnImagesClickListener = onImagesClickListener;
        mDisplaySize = displaySize;
        mPicasso = new Picasso.Builder(context).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        }).build();
    }

    void setMediaContents(List<MediaContent> mediaContents) {
        if (mediaContents == null || mediaContents.size() == 0) return;
        mMediaContents.clear();
        mMediaContents.addAll(mediaContents);
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(mInflater.inflate(R.layout.item_gallery_image, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        holder.bindImage(mMediaContents.get(position));
    }

    @Override
    public void onViewRecycled(ImageViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onDetach();
    }

    @Override
    public int getItemCount() {
        return mMediaContents.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        FrameLayout mFrameLayout;
        ImageView mImageView;
        MediaContent mMediaContent;
        File mFile;

        ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.image_view);
            mFrameLayout = itemView.findViewById(R.id.layout_container);
        }

        void bindImage(MediaContent mediaContent) {
            mMediaContent = mediaContent;
            mFile = new File(mediaContent.getImage());
            ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
            layoutParams.height = mDisplaySize.x / 3;
            layoutParams.width = mDisplaySize.x / 3;
            mImageView.setLayoutParams(layoutParams);
            setUpActionListeners();
            setImageWithRoundCorners(mImageView);
            itemView.findViewById(R.id.image_view_play_video)
                    .setVisibility(mMediaContent.getMediaType().equals(MediaContent.CATEGORY_VIDEO) ?
                            View.VISIBLE : View.GONE);
            if (mMediaContentsSelected.contains(String.valueOf(getAdapterPosition()))) {
                mFrameLayout.setBackground(ContextCompat.getDrawable(mFrameLayout.getContext(), R.drawable.shape_container_view_selected));
            } else {
                mFrameLayout.setBackground(ContextCompat.getDrawable(mFrameLayout.getContext(), R.drawable.shape_container_view_unselected));
            }
        }

        private void setImageWithRoundCorners(ImageView imageView) {
                int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
                mPicasso
                        .load(mFile)
                        .fit()
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(cornerRadius, 0))
                        .placeholder(R.color.black)
                        .into(imageView);
        }

        private void setUpActionListeners() {
            mFrameLayout.setOnTouchListener(mOnTouchListener);
            mFrameLayout.setOnClickListener(mOnClickListener);
        }

        View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.findViewById(R.id.view_hider).setVisibility(View.VISIBLE);
                        break;
                    case MotionEvent.ACTION_UP:
                        v.findViewById(R.id.view_hider).setVisibility(View.GONE);
                        break;
                }
                return false;
            }
        };

        View.OnClickListener mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMediaContentsSelected.contains(String.valueOf(getAdapterPosition()))) {
                    v.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.shape_container_view_unselected));
                    mMediaContentsSelected.remove(String.valueOf(getAdapterPosition()));
                    mOnImagesClickListener.onRemoveImageClick(getAdapterPosition());
                } else {
                    v.setBackground(ContextCompat.getDrawable(v.getContext(), R.drawable.shape_container_view_selected));
                    mMediaContentsSelected.add(String.valueOf(getAdapterPosition()));
                    mOnImagesClickListener.onAddImageClick(getAdapterPosition());
                }
            }
        };

        void onDetach() {
            mPicasso.invalidate(mFile);
        }
    }
}
