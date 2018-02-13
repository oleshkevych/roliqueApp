package io.rolique.cameralibrary.screens.gallery;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import timber.log.Timber;

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
    final Activity mActivity;

    interface OnImagesClickListener {
        void onAddImageClick(int position);

        void onRemoveImageClick(int position);
    }

    private OnImagesClickListener mOnImagesClickListener;

    ImagesAdapter(Activity activity,
                  OnImagesClickListener onImagesClickListener,
                  Point displaySize) {
        mActivity = activity;
        mInflater = LayoutInflater.from(activity);
        mMediaContents = new ArrayList<>();
        mMediaContentsSelected = new ArrayList<>();
        mOnImagesClickListener = onImagesClickListener;
        mDisplaySize = displaySize;
        mPicasso = new Picasso.Builder(activity).listener(new Picasso.Listener() {
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

    private void createImage(final int contentPosition) {
        new Thread() {
            @Override
            public void run() {
                Bitmap thumb = ThumbnailUtils.createVideoThumbnail(mMediaContents.get(contentPosition).getVideo(),
                        MediaStore.Images.Thumbnails.MINI_KIND);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                File file = getPreviewFile();
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(stream.toByteArray());
                    fos.close();
                } catch (Exception e) {
                    Timber.e(e);
                }
                mMediaContents.get(contentPosition).setImage(file.getAbsolutePath());
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        notifyItemChanged(contentPosition);
                    }
                });
            }
        }.start();


    }

    @NonNull
    private File getPreviewFile() {
        File mediaStorageDir = new File(mInflater.getContext().getCacheDir(), "data");
        if (!mediaStorageDir.mkdir() && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory");
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String stringMediaType = ".jpg";
        String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + stringMediaType;
        return new File(path);
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
            if (mediaContent.getImage() == null) {
                createImage(getAdapterPosition());
                mFile = getPreviewFile();
            } else {
                mFile = new File(mediaContent.getImage());
            }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                mPicasso
                        .load(mFile)
                        .fit()
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(cornerRadius, 0))
                        .placeholder(R.drawable.ic_placeholder_grey_160dp)
                        .into(imageView);
            else
                mPicasso
                        .load(mFile)
                        .fit()
                        .centerCrop()
                        .transform(new RoundedCornersTransformation(cornerRadius, 0))
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
