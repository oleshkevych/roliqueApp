package io.rolique.roliqueapp.screens.chat.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.glide.GlideApp;

/**
 * Created by Volodymyr Oleshkevych on 10/19/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class PreviewAdapter extends RecyclerView.Adapter<PreviewAdapter.ImageViewHolder> {

    private LayoutInflater mInflater;
    private List<Media> mMedias;

    public interface OnItemClickListener {
        void onImageClick(int position);

        void onRemoveClick(int position);
    }

    private OnItemClickListener mOnItemClickListener;

    public PreviewAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        mMedias = new ArrayList<>();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public void setMedias(List<Media> medias) {
        if (medias == null || medias.size() == 0) return;
        mMedias.clear();
        mMedias.addAll(medias);
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mMedias.remove(position);
        notifyItemRemoved(position);
    }

    public void clearItems() {
        mMedias.clear();
        notifyDataSetChanged();
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageViewHolder(mInflater.inflate(R.layout.item_preview_image, parent, false));
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

        ImageView mImageView;
        Media mMedia;

        ImageViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(io.rolique.cameralibrary.R.id.image_view);
        }

        void bindImage(Media mediaContent) {
            mMedia = mediaContent;
            setUpActionListeners();
            setImageWithRoundCorners(mImageView, mediaContent.getImageUrl());
            itemView.findViewById(io.rolique.cameralibrary.R.id.image_view_play_video)
                    .setVisibility(mMedia.isVideo() ? View.VISIBLE : View.GONE);
        }

        private void setImageWithRoundCorners(ImageView imageView, String image) {
            int cornerRadius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, itemView.getContext().getResources().getDisplayMetrics());
            GlideApp.with(imageView.getContext())
                    .load(image)
                    .apply(new RequestOptions().transforms(new CenterCrop(), new RoundedCorners(cornerRadius)))
                    .placeholder(R.color.grey_300)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(imageView);
        }

        private void setUpActionListeners() {
            itemView.findViewById(io.rolique.cameralibrary.R.id.layout_delete).setOnClickListener(mOnDeleteClickListener);
            itemView.findViewById(io.rolique.cameralibrary.R.id.image_button_delete).setOnClickListener(mOnDeleteClickListener);
            mImageView.setOnClickListener(mOnImageClickListener);
        }

        View.OnClickListener mOnDeleteClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onRemoveClick(getAdapterPosition());
            }
        };

        View.OnClickListener mOnImageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onImageClick(getAdapterPosition());
            }
        };
    }
}
