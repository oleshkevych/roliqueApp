package io.rolique.roliqueapp.screens.camera;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTouch;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.imageViewer.ImageViewerActivity;
import io.rolique.roliqueapp.util.ui.UiUtil;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public abstract class CameraBaseActivity extends BaseActivity implements CameraContract.View {

    static final int RC_CAMERA_PERMISSION = 101;
    static final String EXTRA_IS_RADIATOR_IMAGES = "EXTRA_IS_RADIATOR_IMAGES";

    private static final HashSet<String> UNSUPPORTED_CAMERA2API_MODELS = new HashSet<>(Arrays.asList(
            "SM-G920F",
            "SM-G920I",
            "SM-G920X",
            "SM-G920R7",
            "SM-G920F",
            "SAMSUNG-SM-G920AZ",
            "SAMSUNG-SM-G920A",
            "SM-G920W8",
            "SM-G9200",
            "SM-G9208",
            "SM-G9209",
            "SM-G920K",
            "SM-G920L",
            "SM-G920R6",
            "SM-G920T1",
            "SM-G920S",
            "SM-G920P",
            "SM-S906L",
            "SM-S907VL",
            "SM-G920T",
            "SM-G920R4",
            "SM-G920V",
            "SAMSUNG-SM-G890A",
            "SM-G925F",
            "SM-G925I",
            "SM-G925X",
            "SM-G925R7",
            "SAMSUNG-SM-G925A",
            "SM-G925W8",
            "SM-G9250",
            "SM-G925K",
            "SM-G925L",
            "SM-G925R6",
            "SM-G925S",
            "SM-G925P",
            "SM-G925T",
            "SM-G925R4",
            "SM-G925V",
            "SM-G9287",
            "SM-G9287C",
            "SM-G928C",
            "SM-G928F",
            "SM-G928G",
            "SM-G928I",
            "SM-G928X",
            "SAMSUNG-SM-G928A",
            "SM-G928W8",
            "SM-G9280",
            "SM-G928K",
            "SM-G928N0",
            "SM-G928L",
            "SM-G928S",
            "SM-G928P",
            "SM-G928T",
            "SM-G928V",
            "HTC One M9",
            "HTC_0PJA10",
            "HTC_M9u",
            "HTC6535LRA",
            "HTC6535LVW",
            "HTC M9pt",
            "HTC One M9PLUS",
            "HTC 0PK71",
            "HTC_M9pw",
            "HTC 0PK71",
//            "SM-G900H",
//            "SM-G930F",
//            "ALE-L21",
//            "Nexus 5X",
            "HTC One M9PLUS_Prime Camera Edition"
    ));

    public static Intent getStartIntent(Context context) {
        if (UNSUPPORTED_CAMERA2API_MODELS.contains(Build.MODEL)) {
            return Camera1Activity.getStartIntent(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Camera2Activity.getStartIntent(context);
        } else {
            return Camera1Activity.getStartIntent(context);
        }
    }

    @BindView(R.id.recycler_view_images) RecyclerView mImagesRecyclerView;
    @BindView(R.id.button_capture) ImageButton mCaptureButton;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    protected int mDisplayOrientation;
    protected boolean mIsTakingPicture;
    protected List<Media> mMedias = new ArrayList<>();
    private ImagesAdapter mImagesAdapter;
    int mMinSwipeDistance;
    private int mOneDp;
    float mDeltaY;
    float mDeltaX;
    boolean mIsActionCalled;
    float mStartPositionY;
    float mStartPositionX;
    boolean mIsImagesForRadiator;
    int mImagesCount;
    public boolean mIsFacingCameraOn;

    @Inject
    protected CameraPresenter mPresenter;

    protected void inject() {
        DaggerCameraComponent.builder()
                .cameraPresenterModule(new CameraPresenterModule(CameraBaseActivity.this))
                .build()
                .inject(CameraBaseActivity.this);
    }

    protected abstract void takePicture();

    protected abstract void switchCamera();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mMinSwipeDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        mOneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        setUpToolbar();
        setUpImagesRecyclerView();
    }

    private void setUpToolbar() {
        mToolbar.setVisibility(mIsImagesForRadiator ? View.VISIBLE : View.GONE);
    }


    private void setUpImagesRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(CameraBaseActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mImagesRecyclerView.setLayoutManager(linearLayoutManager);
        mImagesAdapter = new ImagesAdapter(CameraBaseActivity.this, mMinSwipeDistance * 2);
        mImagesRecyclerView.setAdapter(mImagesAdapter);
        mImagesAdapter.setOnImagesClickListener(mOnImagesClickListener);
    }

    private ImagesAdapter.OnImagesClickListener mOnImagesClickListener = new ImagesAdapter.OnImagesClickListener() {
        @Override
        public void onImageClick(ImageView imageView, Media media) {
            ImageViewerActivity.start(CameraBaseActivity.this, imageView, media);
        }

        @Override
        public void onRemoveClick(Media media) {
            mMedias.remove(media);
            mPresenter.removeFile(media.getImageUrl());
            updateImagesInPreview();
            if (mMedias.size() == 0) {
                toggleImagesRecyclerView();
            }
        }
    };

    void toggleHeightImagesRecyclerView() {
        RecyclerHeightAnimation animation = new RecyclerHeightAnimation(mImagesRecyclerView, false);
        animation.setDuration(250);
        animation.setInterpolator(new LinearInterpolator());
        mImagesRecyclerView.startAnimation(animation);
    }

    private void updateImagesInPreview() {
        ImageView imageView = getViewById(R.id.image_button);
        imageView.setVisibility(mMedias.size() == 0 ? View.GONE : View.VISIBLE);
        changeToggleButtonVisibility(R.id.button_main_size_toggle, mMedias.size() != 0);
        if (mMedias.size() > 0)
            UiUtil.setImageWithRoundCorners(imageView, mMedias.get(mMedias.size() - 1).getImageUrl());

        TextView imagesCountTextView = getViewById(R.id.text_view_images_count);
        imagesCountTextView.setVisibility(mMedias.size() == 0 ? View.GONE : View.VISIBLE);
        LinearLayout toggleButtonLayout = getViewById(R.id.layout_size_toggle);
        toggleButtonLayout.setVisibility(mMedias.size() == 0 ? View.GONE : View.VISIBLE);
        imagesCountTextView.setText(String.valueOf(mMedias.size()));
        mImagesAdapter.setMedias(mMedias);
    }

    void changeToggleButtonVisibility(@IdRes int viewId, boolean isVisible) {
        ImageButton toggleButton = getViewById(viewId);
        toggleButton.setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    void changeToggleButtonImage(@IdRes int viewId, @DrawableRes int res) {
        ImageButton toggleButton = getViewById(viewId);
        toggleButton.setImageResource(res);
    }

    void toggleImagesRecyclerView() {
        if (mMedias.size() > 0 || mImagesRecyclerView.getLayoutParams().height > mOneDp) {
            RecyclerHeightAnimation animation = new RecyclerHeightAnimation(mImagesRecyclerView, true);
            animation.setDuration(250);
            animation.setInterpolator(new LinearInterpolator());
            mImagesRecyclerView.startAnimation(animation);
        }
    }

    @OnClick(R.id.image_view_camera_switcher)
    void onCameraSwitchClick() {
        switchCameraInView();
    }

    @OnClick(R.id.image_view_camera_switcher_toolbar)
    void onCameraSwitchToolbarClick() {
        switchCameraInView();
    }

    private void switchCameraInView() {
        ImageView imageView = getViewById(mImagesCount < 3 ? R.id.image_view_camera_switcher_toolbar : R.id.image_view_camera_switcher);
        imageView.setImageResource(mIsFacingCameraOn ? R.drawable.ic_camera_front_white_24dp : R.drawable.ic_camera_rear_white_24dp);
        mIsFacingCameraOn = !mIsFacingCameraOn;
        switchCamera();
    }

    @OnClick(R.id.content_controls)
    void onContentClick() {
    }

    @OnClick(R.id.content_camera_preview)
    void onCameraPreviewContentClick() {
        takePicture();
    }

    @OnClick(R.id.button_capture)
    void OnCaptureButtonClick() {
        takePicture();
    }

    @OnClick({R.id.image_button, R.id.text_view_images_count, R.id.button_main_size_toggle})
    void onHidingToggleClick() {
        toggleImagesRecyclerView();
    }

    @OnClick(R.id.button_additional_size_toggle)
    void onSizeToggleClick() {
        toggleHeightImagesRecyclerView();
    }

    @OnClick(R.id.image_view_done)
    void OnDoneButtonClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(getString(R.string.extra_camera_images), (ArrayList<? extends Parcelable>) mMedias);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnTouch({R.id.content_camera_preview, R.id.recycler_view_images})
    boolean onPreviewTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDeltaY = 0;
                mDeltaX = 0;
                mIsActionCalled = false;
                mStartPositionY = event.getY();
                mStartPositionX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                mDeltaY = mStartPositionY - event.getY();
                mDeltaX = mStartPositionX - event.getX();
                if (Math.abs(mDeltaX) > Math.abs(mDeltaY)) break;
                if (Math.abs(mDeltaY) > mMinSwipeDistance) {
                    mIsActionCalled = true;
                    if (mImagesRecyclerView.getHeight() == mMinSwipeDistance * 2 && mDeltaY > 0 ||
                            mImagesRecyclerView.getHeight() == mMinSwipeDistance * 3 && mDeltaY < 0) {
                        toggleHeightImagesRecyclerView();
                    } else if (mImagesRecyclerView.getHeight() == mOneDp && mDeltaY > 0 ||
                            mImagesRecyclerView.getHeight() > mOneDp && mDeltaY < 0) {
                        toggleImagesRecyclerView();
                    }
                }
                break;
        }
        return mIsActionCalled;
    }

    protected File getOutputMediaFile() {
        File sdCard = getExternalFilesDir("images");
        File mediaStorageDir = new File(sdCard, "Masshelden");
        mediaStorageDir.mkdir();
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        return new File(path);
    }

    @Override
    public void showSavedFileInView(File file, int height, int width) {
        Timber.d("showSavedFileInView: " + file + " - exists " + file.exists());
        //TODO: add video checker
        Media media = new Media(file.getPath(), height, width, Media.CATEGORY_IMAGE);
        mMedias.add(media);
        mImagesCount++;
        updateImagesInPreview();
    }

    @Override
    public void showErrorFileSaving(Throwable throwable) {
        Timber.d(throwable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation();
    }

    @Override
    protected void onPause() {
        mPresenter.stop();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        for (Media media : mMedias)
            mPresenter.removeFile(media.getImageUrl());
        super.onBackPressed();
    }

    class RecyclerHeightAnimation extends Animation {

        private View mView;
        private int mInitialHeight;
        private final boolean mIsToggleHiding;
        private boolean mIsButtonsToggled;

        RecyclerHeightAnimation(View view, boolean isToggleHiding) {
            mIsToggleHiding = isToggleHiding;
            mView = view;
            int currentHeight = mView.getHeight();
            if (mIsToggleHiding) {
                if (currentHeight > mOneDp) {
                    mInitialHeight = mOneDp;
                } else {
                    mInitialHeight = mMinSwipeDistance * 2;
                }
            } else if (currentHeight == mMinSwipeDistance * 2) {
                mInitialHeight = mMinSwipeDistance * 3;
            } else {
                mInitialHeight = mMinSwipeDistance * 2;
            }
        }

        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            if (interpolatedTime < 0.5) mIsButtonsToggled = false;
            if (mIsToggleHiding) {
                if (mInitialHeight > mOneDp) {
                    hidePicturesPreview(interpolatedTime);
                } else {
                    showPicturesPreviewInStandardSize(interpolatedTime);
                }
            } else if (mInitialHeight == mMinSwipeDistance * 3) {
                changePicturesPreviewSizeToStandard(interpolatedTime);
            } else {
                changePicturesPreviewSizeToAdditional(interpolatedTime);
            }
            mImagesAdapter.updateAdapter(mView.getLayoutParams().height);
            mView.requestLayout();
        }

        private void hidePicturesPreview(float interpolatedTime) {
            mView.getLayoutParams().height = interpolatedTime == 1
                    ? mInitialHeight
                    : (int) (mInitialHeight * interpolatedTime);
            mView.setAlpha(interpolatedTime);
            if (interpolatedTime > 0.75 && !mIsButtonsToggled) {
                mIsButtonsToggled = true;
                changeToggleButtonImage(R.id.button_main_size_toggle, R.drawable.ic_arrow_down_white_32dp);
                changeToggleButtonVisibility(R.id.button_additional_size_toggle, true);
                changeToggleButtonImage(R.id.button_additional_size_toggle, R.drawable.ic_arrow_up_white_16dp);
            }
        }

        private void showPicturesPreviewInStandardSize(float interpolatedTime) {
            mView.getLayoutParams().height = interpolatedTime == 1
                    ? mInitialHeight
                    : (int) (mView.getLayoutParams().height * (1 - interpolatedTime));
            mView.setAlpha(1 - interpolatedTime);
            if (interpolatedTime > 0.75 && !mIsButtonsToggled) {
                mIsButtonsToggled = true;
                changeToggleButtonVisibility(R.id.button_additional_size_toggle, false);
                changeToggleButtonImage(R.id.button_main_size_toggle, R.drawable.ic_arrow_up_white_32dp);
            }
        }

        private void changePicturesPreviewSizeToStandard(float interpolatedTime) {
            mView.getLayoutParams().height = interpolatedTime == 1
                    ? mInitialHeight
                    : (int) (mMinSwipeDistance * 2 + mMinSwipeDistance * interpolatedTime);
            if (interpolatedTime > 0.75 && !mIsButtonsToggled) {
                mIsButtonsToggled = true;
                changeToggleButtonImage(R.id.button_additional_size_toggle, R.drawable.ic_arrow_down_white_16dp);
            }
        }

        private void changePicturesPreviewSizeToAdditional(float interpolatedTime) {
            mView.getLayoutParams().height = interpolatedTime == 1
                    ? mInitialHeight
                    : (int) (mMinSwipeDistance * 3 - mMinSwipeDistance * interpolatedTime);
            if (interpolatedTime > 0.75 && !mIsButtonsToggled) {
                mIsButtonsToggled = true;
                changeToggleButtonImage(R.id.button_additional_size_toggle, R.drawable.ic_arrow_up_white_16dp);
            }
        }

        @Override
        public void setDuration(long durationMillis) {
            super.setDuration(durationMillis);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }
}
