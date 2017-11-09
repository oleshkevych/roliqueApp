package io.rolique.cameralibrary.screens.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v13.app.ActivityCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.rolique.cameralibrary.BaseActivity;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/6/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class GalleryActivity extends BaseActivity {

    private static final String EXTRA_SINGLE_PHOTO_MODE = "SINGLE_PHOTO_MODE";
    private static final String EXTRA_VIDEO_ENABLED = "VIDEO_ENABLED";
    private static final int RC_PERMISSION = 101;

    private static String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static Intent getStartIntent(Context context, boolean isVideoEnabled, boolean isSinglePhoto) {
        Intent intent = new Intent(context, GalleryActivity.class);
        intent.putExtra(EXTRA_SINGLE_PHOTO_MODE, isSinglePhoto);
        intent.putExtra(EXTRA_VIDEO_ENABLED, isVideoEnabled);
        return intent;
    }

    List<MediaContent> mAvailableMediaContents = new ArrayList<>();
    List<MediaContent> mSelectedMediaContents = new ArrayList<>();
    ImagesAdapter mAdapter;

    ImageView mPreviewImageView;
    ImageView mPlayVideoImageView;
    TextView mImagesCountTextView;
    FloatingActionButton mDoneButton;
    RecyclerView mRecyclerView;
    boolean mIsSinglePhoto;
    boolean mIsVideoEnabled;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        mIsSinglePhoto = getIntent().getBooleanExtra(EXTRA_SINGLE_PHOTO_MODE, false);
        mIsVideoEnabled = getIntent().getBooleanExtra(EXTRA_VIDEO_ENABLED, false);
        mPreviewImageView = getViewById(R.id.image_view_preview);
        mPlayVideoImageView = getViewById(R.id.image_view_play_video);
        mDoneButton = getViewById(R.id.button_done);
        mImagesCountTextView = getViewById(R.id.text_view_images_count);
        mRecyclerView = getViewById(R.id.recycler_view_images);
        setUpToolbar();
        setUpRecyclerView(mRecyclerView);
    }

    @Override
    protected void inject() {
        if (lacksPermissions(PERMISSIONS)) {
            requestCameraPermission();
            return;
        }
        new Thread() {
            @Override
            public void run() {
                mAvailableMediaContents = getListFiles();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.setMediaContents(mAvailableMediaContents);
                        findViewById(R.id.progress_layout).setVisibility(View.GONE);
                    }
                });
            }
        }.start();
    }

    private List<MediaContent> getListFiles() {
        List<MediaContent> availableMediaContents = new ArrayList<>();
        // which image properties are we querying
        String[] projection = new String[]{
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.HEIGHT,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.DATA
        };
        availableMediaContents.addAll(getImages(projection));
        if (mIsVideoEnabled)
            availableMediaContents.addAll(getVideos(projection));
        return sortMedias(availableMediaContents);
    }

    private List<MediaContent> getImages(String[] projection) {
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        Cursor cur = getContentResolver().query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );
        return getData(cur);
    }

    private List<MediaContent> getData(Cursor cur) {
        List<MediaContent> mediaContents = new ArrayList<>();
        if (cur.moveToFirst()) {
            String path;
            String date;
            String height;
            String width;
            int pathColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);

            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            int widthColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.WIDTH);
            int heightColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.HEIGHT);

            do {
                path = cur.getString(pathColumn);
                date = cur.getString(dateColumn);
                width = cur.getString(widthColumn);
                height = cur.getString(heightColumn);

                try {
                    mediaContents.add(new MediaContent.Builder()
                            .setHeight(Integer.valueOf(height))
                            .setWidth(Integer.valueOf(width))
                            .setImage(path)
                            .setMediaType(MediaContent.CATEGORY_IMAGE)
                            .setDate(Long.valueOf(date))
                            .create());
                } catch (Exception e) {
                    Timber.e(" path=" + path
                            + "  date_taken=" + date
                            + "  height_taken=" + height
                            + "  width_taken=" + width);
                }
            } while (cur.moveToNext());
        }
        return mediaContents;
    }

    private List<MediaContent> getVideos(String[] projection) {
        List<MediaContent> availableVideoMediaContents = new ArrayList<>();
        Uri videos = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = getContentResolver().query(videos,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                null        // Ordering
        );

        for (MediaContent mediaContent : getData(cursor)) {
            availableVideoMediaContents.add(new MediaContent.Builder()
                    .setHeight(mediaContent.getHeight())
                    .setWidth(mediaContent.getWidth())
                    .setVideo(mediaContent.getImage())
                    .setImage(null)
                    .setMediaType(MediaContent.CATEGORY_VIDEO)
                    .setDate(mediaContent.getDate())
                    .create());
        }
        return availableVideoMediaContents;
    }

    private List<MediaContent> sortMedias(List<MediaContent> availableMediaContents) {
        Collections.sort(availableMediaContents, new MediaSorter());
        return availableMediaContents;
    }

    private void setUpToolbar() {
        findViewById(R.id.image_view_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mDoneButton.setOnClickListener(mOnDoneClickListener);
        updatePreview();
    }

    private void updatePreview() {
        if (mSelectedMediaContents.size() == 0) {
            mPreviewImageView.setVisibility(View.GONE);
            mPlayVideoImageView.setVisibility(View.GONE);
            mImagesCountTextView.setVisibility(View.GONE);
            mDoneButton.setVisibility(View.GONE);
            return;
        }
        MediaContent mediaContent = mSelectedMediaContents.get(mSelectedMediaContents.size() - 1);
        if (mediaContent.getImage() == null)
            mediaContent.setImage(createImage(mediaContent.getVideo(), mediaContent.getWidth(), mediaContent.getHeight()));
        mPreviewImageView.setVisibility(View.VISIBLE);
        mDoneButton.setVisibility(View.VISIBLE);
        mPlayVideoImageView.setVisibility(mediaContent.isVideo() ? View.VISIBLE : View.GONE);
        mImagesCountTextView.setVisibility(View.VISIBLE);
        mImagesCountTextView.setText(String.valueOf(mSelectedMediaContents.size()));
        setImageWithRoundCorners(mPreviewImageView, mediaContent.getImage());
    }

    private void setImageWithRoundCorners(final ImageView imageView, String path) {
        File file = new File(path);
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        Picasso picasso = new Picasso.Builder(imageView.getContext()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        }).build();
        picasso
                .load(file)
                .fit()
                .centerCrop()
                .transform(new RoundedCornersTransformation(cornerRadius, 0))
                .placeholder(R.color.grey_300)
                .into(imageView);
    }

    View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);
            for (MediaContent mediaContent : mSelectedMediaContents)
                if (mediaContent.getImage() == null)
                    mediaContent.setImage(createImage(mediaContent.getVideo(), mediaContent.getWidth(), mediaContent.getHeight()));
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(getString(R.string.extra_camera_images), (ArrayList<? extends Parcelable>) mSelectedMediaContents);
            setResult(RESULT_OK, intent);
            finish();
        }
    };

    private String createImage(String videoPath, int width, int height) {
        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(videoPath,
                MediaStore.Images.Thumbnails.MINI_KIND);
        Bitmap scaled = Bitmap.createScaledBitmap(thumb, width / 8, height / 8, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaled.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        File file = getPreviewFile();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(stream.toByteArray());
            fos.close();
        } catch (Exception e) {
            Timber.e(e);
        }
        return file.getAbsolutePath();
    }

    private File getPreviewFile() {
        File mediaStorageDir = new File(getCacheDir(), "data");
        if (!mediaStorageDir.mkdir() && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String stringMediaType = ".jpg";
        String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + stringMediaType;
        return new File(path);
    }

    private void setUpRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new GridLayoutManager(GalleryActivity.this, 3, RecyclerView.VERTICAL, false));
        Point displaySize = new Point();
        getWindowManager().getDefaultDisplay().getSize(displaySize);
        mAdapter = new ImagesAdapter(GalleryActivity.this, mOnImagesClickListener, displaySize);
        recyclerView.setAdapter(mAdapter);
    }

    ImagesAdapter.OnImagesClickListener mOnImagesClickListener = new ImagesAdapter.OnImagesClickListener() {
        @Override
        public void onAddImageClick(int position) {
            mSelectedMediaContents.add(mAvailableMediaContents.get(position));
            if (mIsSinglePhoto) mDoneButton.performClick();
            else updatePreview();
        }

        @Override
        public void onRemoveImageClick(int position) {
            for (int i = 0; i < mSelectedMediaContents.size(); i++)
                if (mSelectedMediaContents.get(i).getImage().equals(mAvailableMediaContents.get(position).getImage()))
                    mSelectedMediaContents.remove(i);
            updatePreview();
        }
    };

    private class MediaSorter implements Comparator<MediaContent> {

        @Override
        public int compare(MediaContent o1, MediaContent o2) {
            return o1.getDate() < o2.getDate() ? 1 : -1;
        }
    }

    boolean lacksPermissions(String[] permissions) {
        for (String permission : permissions)
            if (ActivityCompat.checkSelfPermission(GalleryActivity.this, permission) != PackageManager.PERMISSION_GRANTED)
                return true;
        return false;
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(GalleryActivity.this, PERMISSIONS, RC_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_PERMISSION) {
            if (lacksPermissions(PERMISSIONS)) {
                Toast.makeText(GalleryActivity.this, R.string.activity_camera_permissions_error, Toast.LENGTH_LONG).show();
                finish();
            } else {
                inject();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}