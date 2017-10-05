package io.rolique.cameralibrary.screens.camera;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import io.rolique.cameralibrary.R;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@SuppressWarnings("deprecation")
public class Camera1Activity extends CameraBaseActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, Camera1Activity.class);
    }

    FrameLayout mContentCameraPreview;

    Camera mCamera;
    CameraPreview mCameraPreview;
    MediaRecorder mMediaRecorder;
    boolean mIsFlashSupported;
    boolean mIsAutoFocusNeed;

    int mCountFocusing;

    public Camera getCamera() {
        return mCamera;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera1);
        mContentCameraPreview = (FrameLayout) mCameraPreviewLayout;
    }

    @Override
    protected void takePicture() {
        Timber.d("getting picture");
        try {
            if (!mIsCameraBusy) {
                if (mIsFlashSupported) setFlashMode();
                mCamera.cancelAutoFocus();
                mCaptureButton.setEnabled(false);
                mCountFocusing++;
                final Camera.Parameters parameters = mCamera.getParameters();
                Timber.e("FOCUS_MODE " + parameters.getFocusMode());
                if (parameters.getFlashMode() != null && parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON)) {
                    mCountFocusing = 4;
                }
                if (!mIsAutoFocusNeed) {
                    mIsCameraBusy = true;
                    mCamera.takePicture(null, null, mPictureCallback);
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success || mCountFocusing > 3) {
                            mIsCameraBusy = true;
                            mCamera.takePicture(null, null, mPictureCallback);
                        } else {
                            Camera.Parameters parameters = camera.getParameters();
                            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                mIsAutoFocusNeed = true;
                            }
                            mCamera.setParameters(parameters);
                            mIsCameraBusy = false;
                            takePicture();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Timber.d("Error Taking Picture: " + e.getMessage());
            e.printStackTrace();
            mCaptureButton.setEnabled(true);
            mIsCameraBusy = false;
            restartCamera();
        }
    }

    Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Toast.makeText(Camera1Activity.this, getResources().getString(R.string.activity_camera_storage_permission_error), Toast.LENGTH_LONG).show();
                return;
            }
            int screenWidth = getResources().getDisplayMetrics().heightPixels;
            int screenHeight = getResources().getDisplayMetrics().widthPixels;
            mCountFocusing = 0;
            mPresenter.savePictureToFile(data, pictureFile, screenWidth, screenHeight, mIsFacingCameraOn, mScreenRotation);
            mCamera.stopPreview();
        }
    };

    public void restartCamera() {
        mCamera.startPreview();
        mCaptureButton.setEnabled(true);
        mCameraPreview.setCameraParameters();
        mIsCameraBusy = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (lacksPermissions(PERMISSIONS)) {
            requestCameraPermission();
            return;
        }
        if (mCamera == null) {
            mCamera = getCameraInstance();
            mCameraPreview = new CameraPreview(this);
            mContentCameraPreview.addView(mCameraPreview);
        }
        checkFlashAvailability();
        Timber.d("Camera started");
    }

    private boolean prepareVideoRecorder(){

        mMediaRecorder = new MediaRecorder();
        setFlashMode();
        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
//        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoEncodingBitRate(1000000);
        mMediaRecorder.setVideoFrameRate(25);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        // Step 4: Set output file
        mFile = getOutputMediaFile();
        mMediaRecorder.setOutputFile(mFile.getPath());

//         Step 5: Set the preview output
        mMediaRecorder.setPreviewDisplay(mCameraPreview.getHolder().getSurface());

        switch (mScreenRotation) {
            case 0:
                mMediaRecorder.setOrientationHint(mIsFacingCameraOn ? 270 : 90);
                break;
            case 90:
                mMediaRecorder.setOrientationHint(180);
                break;
            case 180:
                mMediaRecorder.setOrientationHint(mIsFacingCameraOn ? 90 : 270);
                break;
            case 270:
                mMediaRecorder.setOrientationHint(0);
                break;
        }
        try {
            Timber.d("Starting MediaRecorder");
            mMediaRecorder.prepare();
            Timber.d("Started Recorder");
        } catch (IllegalStateException e) {
            Timber.e("IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Timber.e("IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            mCamera.lock();
            if (mFlashMode == FLASH_MODE_ON) {
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(p);
            }
        }
    }

    @Override
    public void startRecord() {
        if (prepareVideoRecorder()) {
            mMediaRecorder.start();
            mIsCameraBusy = true;
        } else {
            Timber.e("Some troubles with media recorder");
            releaseMediaRecorder();
        }
    }

    @Override
    public void stopRecord(){
        if (mIsCameraBusy) {
            mMediaRecorder.stop();
            releaseMediaRecorder();
            mCamera.lock();

            mIsCameraBusy = false;
            int screenWidth = getResources().getDisplayMetrics().heightPixels;
            int screenHeight = getResources().getDisplayMetrics().widthPixels;
            mPresenter.createVideoPreview(mFile, getPreviewFile(), screenWidth, screenHeight);
            mCamera.stopPreview();
            restartCamera();
        }
    }

    @Override
    public void switchCamera() {
        stopCamera();
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this);
        mContentCameraPreview.addView(mCameraPreview);
        mCamera.startPreview();
        mCaptureButton.setEnabled(true);
        mIsCameraBusy = false;
    }

    private Camera getCameraInstance() {
        Camera c = null;
        if (checkCameraHardware())
            try {
                c = Camera.open(getCameraId());
            } catch (Exception e) {
                Timber.d("Error starting camera: " + e.getMessage());
            }
        setCameraDisplayOrientation(this, getCameraId(), c);

        return c;
    }

    private int getCameraId() {
        int cameraId = -1;
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (mIsFacingCameraOn && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Timber.d("Camera found " + i);
                cameraId = i;
                break;
            }
            if (!mIsFacingCameraOn && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Timber.d("Camera found " + i);
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    private boolean checkCameraHardware() {
        return (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA));
    }

    public void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public boolean checkFlashAvailability() {
        if (mCamera != null) {
            Camera.Parameters parameters = mCamera.getParameters();
            mIsFlashSupported = parameters.getFlashMode() != null;
        }
        setFlashMode();
        return mIsFlashSupported;
    }

    private void setFlashMode() {
        Camera.Parameters parameters = mCamera.getParameters();
        switch (mFlashMode) {
            case FLASH_MODE_AUTO:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                break;
            case FLASH_MODE_ON:
                parameters.setFlashMode(mIsVideoMode ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_ON);
                break;
            case FLASH_MODE_OFF:
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                break;
        }
        mCamera.setParameters(parameters);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRecord();
        stopCamera();
    }

    private void stopCamera() {
        if (mCamera != null) {
            mCamera.cancelAutoFocus();
            mCameraPreview.stopAndRelease();
            mCameraPreview = null;
            mCamera = null;
            mContentCameraPreview.removeAllViews();
        }
    }

    @Override
    public void showSavedPictureInView(File file, int height, int width) {
        super.showSavedPictureInView(file, height, width);
        restartCamera();
    }
}
