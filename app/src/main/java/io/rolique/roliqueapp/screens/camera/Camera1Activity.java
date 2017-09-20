package io.rolique.roliqueapp.screens.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v13.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Surface;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import io.rolique.roliqueapp.R;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@SuppressWarnings("deprecation")
public class Camera1Activity extends CameraBaseActivity {

    public static Intent getStartIntent(Context context) {
        Intent intent = new Intent(context, Camera1Activity.class);
        return intent;
    }

    @BindView(R.id.content_camera_preview) FrameLayout mContentCameraPreview;

    Camera mCamera;
    CameraPreview mCameraPreview;
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
    }

    @Override
    protected void takePicture() {
        Timber.d("getting picture");
        try {
            if (!mIsTakingPicture) {
                mCamera.cancelAutoFocus();
                mCaptureButton.setEnabled(false);
                mCountFocusing++;
                final Camera.Parameters parameters = mCamera.getParameters();
                Timber.e("FOCUS_MODE " + parameters.getFocusMode());
                if (parameters.getFlashMode() != null && parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_ON)) {
                    mIsTakingPicture = true;
                    mCamera.takePicture(null, null, mPictureCallback);
                    return;
                }
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success || mCountFocusing > 3) {
                            mIsTakingPicture = true;
                            mCamera.takePicture(null, null, mPictureCallback);
                        } else {
                            Camera.Parameters parameters = camera.getParameters();
                            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                                mIsAutoFocusNeed = true;
                            }
                            mCamera.setParameters(parameters);
                            mIsTakingPicture = false;
                            takePicture();
                        }
                    }
                });
            }
        } catch (Exception e) {
            Timber.d("Error Taking Picture: " + e.getMessage());
            e.printStackTrace();
            mCaptureButton.setEnabled(true);
            mIsTakingPicture = false;
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
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int screenHeight = getResources().getDisplayMetrics().heightPixels;
            mCountFocusing = 0;
            mPresenter.savePictureToFile(data, pictureFile, screenWidth, screenHeight, mIsFacingCameraOn);
            mCamera.stopPreview();
        }
    };

    public void restartCamera() {
        mCamera.startPreview();
        mCaptureButton.setEnabled(true);
        mCameraPreview.setCameraParameters();
        mIsTakingPicture = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(Camera1Activity.this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
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

    @Override
    public void switchCamera() {
        stopCamera();
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this);
        mContentCameraPreview.addView(mCameraPreview);
        mCamera.startPreview();
        mCaptureButton.setEnabled(true);
        mIsTakingPicture = false;
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
        return mIsFlashSupported;
    }

    private void requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            //TODO: Show OK/Cancel confirmation dialog about camera permission.
        } else {
            ActivityCompat.requestPermissions(Camera1Activity.this, new String[]{Manifest.permission.CAMERA},
                    RC_CAMERA_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == RC_CAMERA_PERMISSION) {
            if (grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                //TODO: Show error message
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
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
    public void showSavedFileInView(File file, int height, int width) {
        super.showSavedFileInView(file, height, width);
        if (mImagesCount != 3)
            restartCamera();
    }
}
