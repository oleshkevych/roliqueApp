package io.rolique.cameralibrary;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.util.ArrayList;
import java.util.List;

import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.camera.CameraBaseActivity;
import io.rolique.cameralibrary.screens.gallery.GalleryActivity;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 9/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 * <p>
 * // * To use Butter Knife in a library, add the plugin to your buildscript:
 * // * <p>
 * // * buildscript {
 * // * repositories {
 * // * mavenCentral()
 * // * }
 * // * dependencies {
 * // * classpath 'com.jakewharton:butterknife-gradle-plugin:8.8.1'
 * // * }
 * // * }
 * <p>
 * android {
 * dexOptions {
 * preDexLibraries = false
 * }
 * }
 * <p>
 * maven {
 * url 'https://oss.sonatype.org/content/repositories/snapshots/'
 * }
 * <p>
 * settings.gradle include ':app', ':cameralibrary'
 * <p>
 * <p>
 * Create new MediaLib() and call method
 * onActivityResult(int requestCode, int resultCode, Intent data)
 * in your Activity.onActivityResult method
 * <p>
 * Set parameters in sets and call MediaLib.startCamera
 */

public class MediaLib {

    private final static int RC_CAMERA = 65526;
    private final static int RC_GALLERY = 65126;

    public final static int LOCAL_APP_FOLDER = 0;
    public final static int GLOBAL_MEDIA_DEFAULT_FOLDER = 1;

    public static final int FLASH_MODE_AUTO = 1;
    public static final int FLASH_MODE_ON = 2;
    public static final int FLASH_MODE_OFF = 3;

    public interface MediaLibListener {
        void onSuccess(List<MediaContent> mediaContents);

        void onEmpty();

        void onError(Exception e);
    }

    private MediaLibListener mMediaLibListener;
    private Activity mActivity;
    private int mStorageCategory = LOCAL_APP_FOLDER;
    private boolean mIsRotationEnable = true;
    private boolean mIsFrontCamera;
    private boolean mIsSingleFrontCamera;
    private boolean mIsSinglePhoto;
    private boolean mIsVideoEnabled;
    private boolean mIsFlashModsSelectable;
    private int mFlashMode = FLASH_MODE_AUTO;

    public MediaLib(Activity activity, MediaLibListener mediaLibListener) {
        mMediaLibListener = mediaLibListener;
        mActivity = activity;
    }

    /**
     * Set where saved files will be stored. Local cash is default
     **/
    public void setStorage(int category) {
        mStorageCategory = category;
    }

    /**
     * Set is landscape mode enabled
     **/
    public void setRotation(boolean isEnabled) {
        mIsRotationEnable = isEnabled;
    }

    /**
     * Set is front camera enabled
     **/
    public void setFrontCamera(boolean isEnabled) {
        mIsFrontCamera = isEnabled;
    }

    /**
     * Set is flash mods selectable
     **/
    public void setSelectableFlash(boolean isSelectable) {
        mIsFlashModsSelectable = isSelectable;
    }

    /**
     * Set flash mod
     **/
    public void setFlashMode(int flashMode) {
        mFlashMode = flashMode;
    }

    /**
     * Set flash mod
     **/
    public void setRecordVideo(boolean isVideoEnabled) {
        mIsVideoEnabled = isVideoEnabled;
    }

    /**
     * Set single photo mod
     **/
    public void setSinglePhoto(boolean isSinglePhoto) {
        mIsSinglePhoto = isSinglePhoto;
    }

    /**
     * Set single photo mod
     **/
    public void setSingleFrontCamera(boolean isSingleFrontCamera) {
        mIsSingleFrontCamera = isSingleFrontCamera;
    }

    public void startCamera() {
        Intent intent = CameraBaseActivity.getStartIntent(mActivity,
                mStorageCategory,
                mIsRotationEnable,
                mIsFrontCamera,
                mIsSinglePhoto,
                mIsSingleFrontCamera,
                mIsFlashModsSelectable,
                mIsVideoEnabled,
                mFlashMode);
        mActivity.startActivityForResult(intent, RC_CAMERA);
    }

    public void startGallery() {
        mActivity.startActivityForResult(GalleryActivity.getStartIntent(mActivity, mIsVideoEnabled, mIsSinglePhoto || mIsSingleFrontCamera), RC_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case RC_CAMERA:
                    List<MediaContent> mediaContents = data.getParcelableArrayListExtra(mActivity.getString(R.string.extra_camera_images));
                    if (mMediaLibListener != null) {
                        if (mediaContents.isEmpty()) mMediaLibListener.onEmpty();
                        else mMediaLibListener.onSuccess(mediaContents);
                    }
            }
        }
    }
}
