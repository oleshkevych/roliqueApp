package io.rolique.cameralibrary;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import io.rolique.cameralibrary.data.model.MediaContent;
import io.rolique.cameralibrary.screens.camera.CameraBaseActivity;

/**
 * Created by Volodymyr Oleshkevych on 9/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

/**
 * To use Butter Knife in a library, add the plugin to your buildscript:
 * <p>
 * buildscript {
 * repositories {
 * mavenCentral()
 * }
 * dependencies {
 * classpath 'com.jakewharton:butterknife-gradle-plugin:8.8.1'
 * }
 * }
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
 **/

public class MediaLib {

    private final static int RC_CAMERA = 101;

    public final static int LOCAL_APP_FOLDER = 0;
    public final static int GLOBAL_MEDIA_DEFAULT_FOLDER = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            LOCAL_APP_FOLDER,
            GLOBAL_MEDIA_DEFAULT_FOLDER
    })

    public @interface SavingStorageCategory {
    }

    public interface MediaLibListener {
        void onSuccess(List<MediaContent> mediaContents);

        void onError(Exception e);
    }

    private MediaLibListener mMediaLibListener;
    private Activity mActivity;
    private @SavingStorageCategory int mStorageCategory;

    public MediaLib(Activity activity, MediaLibListener mediaLibListener) {
        mMediaLibListener = mediaLibListener;
        mActivity = activity;
    }

    public void setStorage(@SavingStorageCategory int category) {
        mStorageCategory = category;
    }

    public void startCamera() {
        Intent intent = CameraBaseActivity.getStartIntent(mActivity, mStorageCategory);
        mActivity.startActivityForResult(intent, RC_CAMERA);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case RC_CAMERA:
                    List<MediaContent> mediaContents = data.getParcelableArrayListExtra(mActivity.getString(R.string.extra_camera_images));
                    if (mMediaLibListener != null)
                        mMediaLibListener.onSuccess(mediaContents);
            }
    }
}
