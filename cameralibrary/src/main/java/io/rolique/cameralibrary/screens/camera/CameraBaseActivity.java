package io.rolique.cameralibrary.screens.camera;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
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
import io.rolique.cameralibrary.BaseActivity;
import io.rolique.cameralibrary.BuildConfig;
import io.rolique.cameralibrary.MediaLib;
import io.rolique.cameralibrary.R;
import io.rolique.cameralibrary.R2;
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
//            "SM-G900H",
//            "SM-G930F",
//            "ALE-L21",
//            "Nexus 5X",
            "HTC One M9PLUS_Prime Camera Edition"
    ));

    static final int RC_CAMERA_PERMISSION = 101;

    static final String EXTRA_STORAGE_CATEGORY = "STORAGE_CATEGORY";

    static final int FLASH_MODE_AUTO = 1;
    static final int FLASH_MODE_ON = 2;
    static final int FLASH_MODE_OFF = 3;

    public static Intent getStartIntent(Context context, @MediaLib.SavingStorageCategory int category) {
        Intent intent = getCameraIntent(context);
        intent.putExtra(EXTRA_STORAGE_CATEGORY, category);
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

    @BindView(R2.id.button_flash) ImageButton mFlashButton;
    @BindView(R2.id.button_camera_switcher) ImageButton mCameraSwitcherButton;
    @BindView(R2.id.recycler_view_images) RecyclerView mImagesRecyclerView;
    @BindView(R2.id.image_view_preview) ImageView mPreviewImageView;
    @BindView(R2.id.image_view_done) ImageView mDoneImageView;
    @BindView(R2.id.button_capture) ImageButton mCaptureButton;

    int mFlashMode = FLASH_MODE_AUTO;
    int mDisplayOrientation;
    boolean mIsTakingPicture;
    List<MediaContent> mMediaContents = new ArrayList<>();
    private ImagesAdapter mImagesAdapter;
    int mMinSwipeDistance;
    private int mOneDp;
    float mDeltaY;
    float mDeltaX;
    boolean mIsActionCalled;
    float mStartPositionY;
    float mStartPositionX;
    boolean mIsFacingCameraOn;
    @MediaLib.SavingStorageCategory int mStorageCategory;
    int mScreenRotation;

    @Inject
    protected CameraPresenter mPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        plantTimber();
        mStorageCategory = getIntent().getIntExtra(EXTRA_STORAGE_CATEGORY, 0);
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
        DaggerWrapper
                .getComponent(CameraBaseActivity.this)
                .inject(CameraBaseActivity.this);
    }

    protected abstract void takePicture();

    protected abstract void switchCamera();

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mMinSwipeDistance = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, getResources().getDisplayMetrics());
        mOneDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
        setUpImagesRecyclerView();
        updateImagesInPreview();
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
            mPresenter.removeFile(mediaContent.getImage());
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
        mPreviewImageView.setVisibility(mMediaContents.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        changeToggleButtonVisibility(R.id.button_main_size_toggle, mMediaContents.size() != 0);
        if (mMediaContents.size() > 0)
            UiUtil.setImageWithRoundCorners(mPreviewImageView, mMediaContents.get(mMediaContents.size() - 1).getImage());

        TextView imagesCountTextView = getViewById(R.id.text_view_images_count);
        imagesCountTextView.setVisibility(mMediaContents.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        LinearLayout toggleButtonLayout = getViewById(R.id.layout_size_toggle);
        toggleButtonLayout.setVisibility(mMediaContents.size() == 0 ? View.INVISIBLE : View.VISIBLE);
        imagesCountTextView.setText(String.valueOf(mMediaContents.size()));
        mImagesAdapter.setMediaContents(mMediaContents);
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

    @OnClick(R2.id.button_camera_switcher)
    void onCameraSwitchClick() {
        switchCameraInView();
    }

    private void switchCameraInView() {
        ImageView imageView = getViewById(R.id.button_camera_switcher);
        imageView.setImageResource(mIsFacingCameraOn ? R.drawable.ic_camera_front_white_24dp : R.drawable.ic_camera_rear_white_24dp);
        mIsFacingCameraOn = !mIsFacingCameraOn;
        switchCamera();
    }

    @OnClick(R2.id.content_controls)
    void onContentClick() {
    }

    @OnClick(R2.id.content_camera_preview)
    void onCameraPreviewContentClick() {
        takePicture();
    }

    @OnClick(R2.id.button_capture)
    void OnCaptureButtonClick() {
        takePicture();
    }

    @OnClick({R2.id.image_view_preview, R2.id.text_view_images_count, R2.id.button_main_size_toggle})
    void onHidingToggleClick(View view) {
        if(view.getVisibility() == View.INVISIBLE) return;
        toggleImagesRecyclerView();
    }

    @OnClick(R2.id.button_additional_size_toggle)
    void onSizeToggleClick(View view) {
        if(view.getVisibility() == View.INVISIBLE) return;
        toggleHeightImagesRecyclerView();
    }

    @OnClick(R2.id.image_view_done)
    void OnDoneButtonClick() {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(getString(R.string.extra_camera_images), (ArrayList<? extends Parcelable>) mMediaContents);
        setResult(RESULT_OK, intent);
        finish();
    }

    private int mFlashCounter = 0;

    @OnClick(R2.id.button_flash)
    void onFlashButtonClick() {
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

    @OnTouch({R2.id.content_camera_preview, R2.id.recycler_view_images})
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
        File sdCard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (isAppLocalStorage())
            sdCard = getExternalFilesDir("images");
        File mediaStorageDir = new File(sdCard, "Rolique");
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

    public boolean isAppLocalStorage() {
        return mStorageCategory == MediaLib.LOCAL_APP_FOLDER;
    }

    @Override
    public void showSavedFileInView(File file, int height, int width) {
        Timber.d("showSavedFileInView: " + file + " - exists " + file.exists());
        //TODO: add video checker
        int heightWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? height : width;
        int widthWithRotation = mScreenRotation == 90 || mScreenRotation == 270 ? width : height;
        MediaContent mediaContent = new MediaContent(file, heightWithRotation, widthWithRotation, MediaContent.CATEGORY_IMAGE);
        mMediaContents.add(mediaContent);
        updateImagesInPreview();
        if (!isAppLocalStorage()) galleryAddPic(file);
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
        SensorOrientationChangeNotifier.getInstance(getApplicationContext()).addListener(mListener);
    }

    @Override
    protected void onPause() {
        mPresenter.stop();
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
        Timber.e("Orientation " + orientation);
        animateRotation(mFlashButton, mScreenRotation, orientation);
        animateRotation(mCameraSwitcherButton, mScreenRotation, orientation);
        animateRotation(mPreviewImageView, mScreenRotation, orientation);
        animateRotation(mCaptureButton, mScreenRotation, orientation);
        animateRotation(mDoneImageView, mScreenRotation, orientation);
        animateRotation(getViewById(R.id.text_view_images_count), mScreenRotation, orientation);
        mScreenRotation = orientation;
    }

    private void animateRotation(View view, int rotationFrom, int rotationTo) {
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
        for (MediaContent mediaContent : mMediaContents)
            mPresenter.removeFile(mediaContent.getImage());
        super.onBackPressed();
    }

    class RecyclerHeightAnimation extends Animation {

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
}
