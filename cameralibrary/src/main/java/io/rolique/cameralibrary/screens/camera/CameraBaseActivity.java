package io.rolique.cameralibrary.screens.camera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
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

import io.rolique.cameralibrary.BaseActivity;
import io.rolique.cameralibrary.BuildConfig;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.imageViewer.ImageViewerActivity;
import io.rolique.cameralibrary.uiUtil.UiUtil;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public abstract class CameraBaseActivity extends BaseActivity implements CameraContract.View {

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
            "D6502",
            "D6503",
            "D6543",
            "SO-03F",
            "D6563",
//            "SM-G900H",
//            "SM-G930F",
//            "ALE-L21",
//            "Nexus 5X",
            "HTC One M9PLUS_Prime Camera Edition"
    ));

    static final int RC_CAMERA_PERMISSION = 101;

    private static final String EXTRA_STORAGE_CATEGORY = "STORAGE_CATEGORY";
    private static final String EXTRA_ROTATION_ENABLED = "ROTATION_ENABLED";
    private static final String EXTRA_FRONT_CAMERA_ENABLED = "FRONT_CAMERA_ENABLED";
    private static final String EXTRA_SINGLE_PHOTO_MODE = "SINGLE_PHOTO_MODE";
    private static final String EXTRA_SINGLE_FRONT_CAMERA = "SINGLE_FRONT_CAMERA";
    private static final String EXTRA_FLASH_MODS_SELECTABLE = "FLASH_MODS_SELECTABLE";
    private static final String EXTRA_DEFAULT_FLASH_MODE = "DEFAULT_FLASH_MODE";

    static final int FLASH_MODE_AUTO = 1;
    static final int FLASH_MODE_ON = 2;
    static final int FLASH_MODE_OFF = 3;

    static String[] PERMISSIONS = {Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,};

    public static Intent getStartIntent(Context context,
                                        int category,
                                        boolean isRotationEnabled,
                                        boolean isFrontCameraEnabled,
                                        boolean isSinglePhotoMode,
                                        boolean isSingleFrontCamera,
                                        boolean isFlashModsSelectable,
                                        int defaultFlashMod) {
        Intent intent = getCameraIntent(context);
        intent.putExtra(EXTRA_STORAGE_CATEGORY, category);
        intent.putExtra(EXTRA_ROTATION_ENABLED, isRotationEnabled);
        intent.putExtra(EXTRA_FRONT_CAMERA_ENABLED, isFrontCameraEnabled);
        intent.putExtra(EXTRA_SINGLE_PHOTO_MODE, isSinglePhotoMode);
        intent.putExtra(EXTRA_SINGLE_FRONT_CAMERA, isSingleFrontCamera);
        intent.putExtra(EXTRA_FLASH_MODS_SELECTABLE, isFlashModsSelectable);
        intent.putExtra(EXTRA_DEFAULT_FLASH_MODE, defaultFlashMod);
        return intent;
    }

    private static Intent getCameraIntent(Context context) {
        if (UNSUPPORTED_CAMERA2API_MODELS.contains(Build.MODEL)) {
            return Camera1Activity.getStartIntent(context);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Camera2Activity.getStartIntent(context);
        } else {
            return Camera1Activity.getStartIntent(context);
        }
    }

    View mCameraPreviewLayout;
    ImageButton mFlashButton;
    ImageButton mCameraSwitcherButton;
    RecyclerView mImagesRecyclerView;
    ImageView mPreviewImageView;
    ImageView mPlayVideoImageView;
    ImageView mDoneImageView;
    ImageButton mCaptureButton;
    ImageButton mVideoButton;
    TextView mImagesCountTextView;

    int mFlashMode = FLASH_MODE_AUTO;
    int mDisplayOrientation;
    boolean mIsCameraBusy;
    List<MediaContent> mMediaContents = new ArrayList<>();
    private ImagesAdapter mImagesAdapter;
    int mMinSwipeDistance;
    int mMinSwipeCaptureButtonDistance;
    private int mOneDp;
    float mDeltaY;
    float mDeltaX;
    boolean mIsActionCalled;
    float mStartPositionY;
    float mStartPositionX;
    boolean mIsFacingCameraOn;
    int mScreenRotation;
    int mFlashCounter;
    boolean mIsVideoMode;
    File mFile;

    private int mStorageCategory;
    private boolean mIsRotationEnable;
    private boolean mIsFrontCameraEnable;
    private boolean mIsSinglePhoto;
    private boolean mIsFlashModsSelectable;
    private int mDefaultFlashMode;
    private boolean mIsSingleFrontCamera;

    protected CameraPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        plantTimber();
        mStorageCategory = getIntent().getIntExtra(EXTRA_STORAGE_CATEGORY, 0);
        mIsRotationEnable = getIntent().getBooleanExtra(EXTRA_ROTATION_ENABLED, false);
        mIsFrontCameraEnable = getIntent().getBooleanExtra(EXTRA_FRONT_CAMERA_ENABLED, false);
        mIsSinglePhoto = getIntent().getBooleanExtra(EXTRA_SINGLE_PHOTO_MODE, false);
        mIsSingleFrontCamera = getIntent().getBooleanExtra(EXTRA_SINGLE_FRONT_CAMERA, false);
        mIsFlashModsSelectable = getIntent().getBooleanExtra(EXTRA_FLASH_MODS_SELECTABLE, false);
        mDefaultFlashMode = getIntent().getIntExtra(EXTRA_DEFAULT_FLASH_MODE, FLASH_MODE_AUTO);
    }

    private void plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }

    protected void inject() {
        new CameraPresenter(CameraBaseActivity.this);
    }

    @Override
    public void setPresenter(CameraPresenter presenter) {
        mPresenter = presenter;
    }

    protected abstract void takePicture();

    protected abstract void startRecord();

    protected abstract void stopRecord();

    protected abstract void switchCamera();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mFlashButton = getViewById(R.id.button_flash);
        mCameraSwitcherButton = getViewById(R.id.button_camera_switcher);
        mImagesRecyclerView = getViewById(R.id.recycler_view_images);
        mPreviewImageView = getViewById(R.id.image_view_preview);
        mPlayVideoImageView = getViewById(R.id.image_view_play_video);
        mDoneImageView = getViewById(R.id.image_view_done);
        mCaptureButton = getViewById(R.id.button_capture);
        mVideoButton = getViewById(R.id.button_video);
        mCameraPreviewLayout = getViewById(R.id.content_camera_preview);
        mImagesCountTextView = getViewById(R.id.text_view_images_count);
        mMinSwipeDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        mMinSwipeCaptureButtonDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
        mOneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        setUpProperties();
        setUpImagesRecyclerView();
        setActionListeners();
        updateImagesInPreview();
        setUpActionButtons();
    }

    private void setUpProperties() {
        mCameraSwitcherButton.setVisibility(mIsFrontCameraEnable ? View.VISIBLE : View.GONE);
        if (!mIsFlashModsSelectable) {
            mFlashButton.setVisibility(View.GONE);
            mFlashMode = mDefaultFlashMode;
        }
        setFlashDrawable();
        if (mIsSingleFrontCamera) {
            mFlashMode = FLASH_MODE_OFF;
            mCameraSwitcherButton.setVisibility(View.GONE);
            mIsFacingCameraOn = true;
            mFlashButton.setVisibility(View.GONE);
        }
        if (mIsSinglePhoto)
            mDoneImageView.setVisibility(View.GONE);
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
        public void onImageClick(ImageView imageView, MediaContent mediaContent) {
            ImageViewerActivity.start(CameraBaseActivity.this, imageView, mediaContent);
        }

        @Override
        public void onRemoveClick(MediaContent mediaContent) {
            mMediaContents.remove(mediaContent);
            mPresenter.removeFile(mediaContent.getImage() == null ? mediaContent.getVideo() : mediaContent.getImage());
            updateImagesInPreview();
            if (mMediaContents.size() == 0) {
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
        toggleVisibility(mPreviewImageView, mMediaContents.size() == 0);
        toggleVisibility(mPlayVideoImageView,
                (mMediaContents.size() == 0 || !mMediaContents.get(mMediaContents.size() - 1)
                        .getMediaType().equals(MediaContent.CATEGORY_VIDEO)));
        changeToggleButtonVisibility(R.id.button_main_size_toggle, mMediaContents.size() != 0);
        if (mMediaContents.size() > 0)
            UiUtil.setImageWithRoundCorners(mPreviewImageView, mMediaContents.get(mMediaContents.size() - 1).getImage());

        toggleVisibility(mImagesCountTextView, mMediaContents.size() == 0);
        LinearLayout toggleButtonLayout = getViewById(R.id.layout_size_toggle);
        toggleButtonLayout.setVisibility(mMediaContents.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        mImagesCountTextView.setText(String.valueOf(mMediaContents.size()));
        mImagesAdapter.setMediaContents(mMediaContents);
    }

    private void toggleVisibility(final View view, boolean isHide) {
        if (isHide) {
            if (mScreenRotation == 0) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
            } else {
                Animation an = new RotateAnimation(calculateRotation(mScreenRotation),
                        calculateRotation(0),
                        view.getPivotX(),
                        view.getPivotY());

                an.setDuration(250);
                an.setRepeatCount(0);
                an.setFillAfter(true);
                an.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        view.clearAnimation();
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                view.startAnimation(an);
            }
        } else {
            view.setVisibility(View.VISIBLE);
            animateRotation(view, 0, mScreenRotation);
        }
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
        if (mMediaContents.size() > 0 || mImagesRecyclerView.getLayoutParams().height > mOneDp) {
            RecyclerHeightAnimation animation = new RecyclerHeightAnimation(mImagesRecyclerView, true);
            animation.setDuration(250);
            animation.setInterpolator(new LinearInterpolator());
            mImagesRecyclerView.startAnimation(animation);
        }
    }

    private void setActionListeners() {
        mCameraSwitcherButton.setOnClickListener(mOnCameraSwitcherClickListener);
        getViewById(R.id.content_controls).setOnClickListener(null);
        mCameraPreviewLayout.setOnClickListener(mOnCameraActionClickListener);
        mCaptureButton.setOnClickListener(mOnCameraActionClickListener);
        mVideoButton.setOnClickListener(mOnCameraActionClickListener);
        mPreviewImageView.setOnClickListener(mOnToggleRecyclerClickListener);
        mImagesCountTextView.setOnClickListener(mOnToggleRecyclerClickListener);
        getViewById(R.id.button_main_size_toggle).setOnClickListener(mOnToggleRecyclerClickListener);
        getViewById(R.id.button_additional_size_toggle).setOnClickListener(mOnToggleSizeClickListener);
        mDoneImageView.setOnClickListener(mOnDoneClickListener);
        mFlashButton.setOnClickListener(mOnFlashClickListener);

        mImagesRecyclerView.setOnTouchListener(mOnPreviewTouchListener);
        mCameraPreviewLayout.setOnTouchListener(mOnPreviewTouchListener);
        mVideoButton.setOnTouchListener(mOnActionButtonTouchListener);
    }

    View.OnClickListener mOnCameraSwitcherClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switchCameraInView();
        }
    };

    private void switchCameraInView() {
        mCameraSwitcherButton.setImageResource(mIsFacingCameraOn ? R.drawable.ic_camera_front_white_24dp : R.drawable.ic_camera_rear_white_24dp);
        mIsFacingCameraOn = !mIsFacingCameraOn;
        toggleVisibility(mFlashButton, mIsFacingCameraOn);
        if (mIsFacingCameraOn) setFlashDrawable();
        else mFlashMode = FLASH_MODE_OFF;
        switchCamera();
    }

    private void setFlashDrawable() {
        if (mIsVideoMode) {
            mFlashCounter = 0;
            mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
            mFlashMode = FLASH_MODE_OFF;
        } else {
            switch (mDefaultFlashMode) {
                case FLASH_MODE_AUTO:
                    mFlashButton.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                    mFlashMode = FLASH_MODE_AUTO;
                    mFlashCounter = 0;
                    break;
                case FLASH_MODE_ON:
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                    mFlashMode = FLASH_MODE_ON;
                    mFlashCounter = 1;
                    break;
                case FLASH_MODE_OFF:
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                    mFlashMode = FLASH_MODE_OFF;
                    mFlashCounter = 2;
                    break;
            }
        }
    }

    View.OnClickListener mOnCameraActionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsVideoMode) onChangeVideoState();
            else takePicture();
        }
    };

    private void onChangeVideoState() {
        if (mIsCameraBusy) {
            stopRecord();
            mFlashButton.setVisibility(mIsFlashModsSelectable ? View.VISIBLE : View.GONE);
        } else {
            startRecord();
            mFlashButton.setVisibility(View.GONE);
        }
        mVideoButton.setImageResource(mIsCameraBusy ? R.drawable.ic_stop_white_48dp : R.drawable.ic_videocam_white_48dp);
    }

    View.OnClickListener mOnToggleRecyclerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getVisibility() == View.INVISIBLE) return;
            toggleImagesRecyclerView();
        }
    };

    View.OnClickListener mOnToggleSizeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getVisibility() == View.INVISIBLE) return;
            toggleHeightImagesRecyclerView();
        }
    };

    View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onDoneClick();
        }
    };

    void onDoneClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(getString(R.string.extra_camera_images), (ArrayList<? extends Parcelable>) mMediaContents);
        setResult(RESULT_OK, intent);
        mPresenter.stop();
        finish();
    }

    View.OnClickListener mOnFlashClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFlashCounter++;
            AnimationSet as = new AnimationSet(true);
            RotateAnimation animation1 = new RotateAnimation(80f, -80f);
            animation1.setRepeatCount(0);
            animation1.setDuration(250);
            animation1.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (mIsVideoMode) {
                        switch (mFlashCounter % 2) {
                            case 1:
                                mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                                mFlashMode = FLASH_MODE_OFF;
                                break;
                            case 0:
                                mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                                mFlashMode = FLASH_MODE_ON;
                                break;
                        }
                    } else {
                        switch (mFlashCounter % 3) {
                            case 0:
                                mFlashButton.setImageResource(R.drawable.ic_flash_auto_white_24dp);
                                mFlashMode = FLASH_MODE_AUTO;
                                break;
                            case 1:
                                mFlashButton.setImageResource(R.drawable.ic_flash_on_white_24dp);
                                mFlashMode = FLASH_MODE_ON;
                                break;
                            case 2:
                                mFlashButton.setImageResource(R.drawable.ic_flash_off_white_24dp);
                                mFlashMode = FLASH_MODE_OFF;
                                break;
                        }
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            as.addAnimation(animation1);

            RotateAnimation animation2 = new RotateAnimation(-80f, 80f);
            animation2.setRepeatCount(0);
            animation2.setDuration(250);
            animation2.setStartOffset(250);
            animation2.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    animateRotation(mFlashButton, 0, mScreenRotation);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            as.addAnimation(animation2);
            mFlashButton.startAnimation(as);
        }
    };

    View.OnTouchListener mOnPreviewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
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
    };

    View.OnTouchListener mOnActionButtonTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mDeltaY = 0;
                    mDeltaX = 0;
                    mStartPositionY = event.getY();
                    mStartPositionX = event.getX();
                    break;
                case MotionEvent.ACTION_UP:
                    mDeltaY = mStartPositionY - event.getY();
                    mDeltaX = mStartPositionX - event.getX();
                    if (Math.abs(mDeltaY) > Math.abs(mDeltaX)) break;
                    if ((mDeltaX) > mMinSwipeCaptureButtonDistance) {
                        if(mIsVideoMode) return true;
                        mCaptureButton.setOnTouchListener(mOnActionButtonTouchListener);
                        mVideoButton.setOnTouchListener(null);
                        mIsVideoMode = true;
                        startAppearAnimation(mVideoButton);
                        startDisappearAnimation(mCaptureButton);
                        setFlashDrawable();
                    } else if (-1 * (mDeltaX) > mMinSwipeCaptureButtonDistance){
                        if(!mIsVideoMode) return true;
                        mCaptureButton.setOnTouchListener(null);
                        mVideoButton.setOnTouchListener(mOnActionButtonTouchListener);
                        mIsVideoMode = false;
                        startAppearAnimation(mCaptureButton);
                        startDisappearAnimation(mVideoButton);
                        setFlashDrawable();
                    }
                    break;
            }
            return true;
        }
    };

    private void startDisappearAnimation(final ImageButton imageButton) {
        imageButton.setClickable(false);
        boolean isVideoButton = imageButton == mVideoButton;
        final float translateToX = isVideoButton ? 70 * mOneDp : - 70 * mOneDp;
        imageButton.setTranslationX(translateToX);
        imageButton.setAlpha(0.5f);
        AnimationSet animationSet = new AnimationSet(true);
        Animation animation1 = new TranslateAnimation(0, translateToX, 0, 0);
        animationSet.addAnimation(animation1);
        Animation animation2 = new AlphaAnimation(1.0f, 0.5f);
        animationSet.addAnimation(animation2);
        animationSet.setDuration(1000);
        imageButton.startAnimation(animationSet);
    }

    private void startAppearAnimation(final ImageButton imageButton) {
        imageButton.setClickable(true);
        boolean isVideoButton = imageButton == mVideoButton;
        final float translateFromX = isVideoButton ?  - 70 * mOneDp :  70 * mOneDp;
        imageButton.setTranslationX(0);
        imageButton.setAlpha(1.0f);
        AnimationSet animationSet = new AnimationSet(true);
        Animation animation1 = new TranslateAnimation(translateFromX, 0, 0, 0);
        animationSet.addAnimation(animation1);
        Animation animation2 = new AlphaAnimation(0.5f, 1.0f);
        animationSet.addAnimation(animation2);
        animationSet.setDuration(1000);
        imageButton.startAnimation(animationSet);
    }

    private void setUpActionButtons() {
        mVideoButton.setAlpha(0.5f);
        mVideoButton.setTranslationX(70 * mOneDp);
    }

    protected File getOutputMediaFile() {
        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (isAppLocalStorage())
            sdCard = new File(Environment.getExternalStorageDirectory(), "data");
        File mediaStorageDir = new File(sdCard, "Rolique");
        if (!mediaStorageDir.mkdir() && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Timber.d("failed to create directory");
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String stringMediaType = mIsVideoMode ? ".mp4" : ".jpg";
        String path = mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + stringMediaType;
        return new File(path);
    }

    public boolean isAppLocalStorage() {
        return mStorageCategory == MediaLib.LOCAL_APP_FOLDER;
    }

    @Override
    public void showSavedPictureInView(File file, int height, int width) {
        Timber.d("showSavedPictureInView: " + file + " - exists " + file.exists());
        int heightWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? height : width;
        int widthWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? width : height;
        MediaContent mediaContent = new MediaContent(file,
                heightWithRotation,
                widthWithRotation,
                MediaContent.CATEGORY_IMAGE);
        mMediaContents.add(mediaContent);
        showSavedInView(file);
    }

    protected void showSavedInView(File file) {
        if (!isAppLocalStorage()) galleryAddPic(file);
        if (mIsSinglePhoto) onDoneClick();
        updateImagesInPreview();
    }

    @Override
    public void showSavedVideoInView(File video, File preview, int height, int width) {
        int heightWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? height : width;
        int widthWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? width : height;
        MediaContent mediaContent = new MediaContent(video,
                preview,
                heightWithRotation,
                widthWithRotation,
                MediaContent.CATEGORY_VIDEO);
        mMediaContents.add(mediaContent);
        showSavedInView(video);
    }

    private void galleryAddPic(File f) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    @Override
    public void showErrorFileSaving(Throwable throwable) {
        Timber.d(throwable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDisplayOrientation = getWindowManager().getDefaultDisplay().getRotation();
        if (mIsRotationEnable)
            SensorOrientationChangeNotifier.getInstance(getApplicationContext()).addListener(mListener);
    }

    @Override
    protected void onPause() {
        mPresenter.stop();
        if (mIsRotationEnable)
            SensorOrientationChangeNotifier.getInstance(getApplicationContext()).remove(mListener);
        super.onPause();
    }

    SensorOrientationChangeNotifier.Listener mListener = new SensorOrientationChangeNotifier.Listener() {
        @Override
        public void onOrientationChange(int orientation) {
            toggleOrientation(orientation);
        }
    };

    protected void toggleOrientation(int orientation) {
        animateRotation(mFlashButton, mScreenRotation, orientation);
        animateRotation(mCameraSwitcherButton, mScreenRotation, orientation);
        animateRotation(mPreviewImageView, mScreenRotation, orientation);
        animateRotation(mPlayVideoImageView, mScreenRotation, orientation);
        animateRotation(mCaptureButton, mScreenRotation, orientation);
        animateRotation(mVideoButton, mScreenRotation, orientation);
        animateRotation(mDoneImageView, mScreenRotation, orientation);
        animateRotation(mImagesCountTextView, mScreenRotation, orientation);
        mScreenRotation = orientation;
    }

    private void animateRotation(View view, int rotationFrom, int rotationTo) {
        if (view.getVisibility() != View.VISIBLE) return;
        Animation an = new RotateAnimation(calculateRotation(rotationFrom),
                calculateRotation(rotationTo),
                view.getPivotX(),
                view.getPivotY());

        an.setDuration(400);
        an.setRepeatCount(0);
        an.setFillAfter(true);
        view.startAnimation(an);
    }

    private int calculateRotation(int rotation) {
        switch (rotation) {
            case 90:
                return -90;
            case 270:
                return 90;
            default:
                return rotation;
        }
    }

    @Override
    public void onBackPressed() {
        for (MediaContent mediaContent : mMediaContents) {
            if (mediaContent.getVideo() != null)
                mPresenter.removeFile(mediaContent.getVideo());
            if (mediaContent.getImage() != null)
                mPresenter.removeFile(mediaContent.getImage());
        }
        super.onBackPressed();
    }

    private class RecyclerHeightAnimation extends Animation {

        private final boolean mIsToggleHiding;
        private View mView;
        private int mInitialHeight;
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

    boolean lacksPermissions(String[] permissions) {
        for (String permission : permissions)
            if (ActivityCompat.checkSelfPermission(CameraBaseActivity.this, permission) != PackageManager.PERMISSION_GRANTED)
                return true;
        return false;
    }

    void requestCameraPermission() {
        ActivityCompat.requestPermissions(CameraBaseActivity.this, PERMISSIONS, RC_CAMERA_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            if (lacksPermissions(PERMISSIONS)) {
                //TODO: Show error message
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    protected File getPreviewFile() {
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
}
