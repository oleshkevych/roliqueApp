package io.rolique.roliqueapp.screens.camera;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import io.rolique.roliqueapp.R;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
@SuppressWarnings("deprecation")
class CameraPreview extends SurfaceView implements SurfaceHolder.Callback, Camera.ErrorCallback {

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private final List<Camera.Size> mSupportedPreviewSizes;
    private Camera1Activity mCamera1Activity;
    private Camera.Size mPreviewSize;

    CameraPreview(Camera1Activity camera1Activity) {
        super(camera1Activity.getApplicationContext());
        mCamera = camera1Activity.getCamera();
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mCamera1Activity = camera1Activity;
        mCamera.setErrorCallback(this);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Timber.d("Error setting camera preview: " + e.getMessage());
            Toast.makeText(mCamera1Activity, mCamera1Activity.getResources().getString(R.string.activity_camera_camera_error), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            mCamera1Activity.finish();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            Timber.d("Error starting camera preview: " + e.getMessage());
        }
        try {
            setCameraParameters();
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCameraParameters() {
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPictureSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                mCamera1Activity.mIsAutoFocusNeed = true;
            }
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                mCamera1Activity.mIsAutoFocusNeed = false;
            }
            if (parameters.getWhiteBalance().contains(Camera.Parameters.WHITE_BALANCE_DAYLIGHT))
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_DAYLIGHT);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(80);
            if (parameters.getColorEffect().contains(Camera.Parameters.EFFECT_WHITEBOARD))
                parameters.setColorEffect(Camera.Parameters.EFFECT_WHITEBOARD);
            if (mCamera1Activity.checkFlashAvailability() && !mCamera1Activity.mIsFacingCameraOn) {
                if (parameters.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                }
            }
            mCamera.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopAndRelease() {
        if (mCamera != null) {
            try {
                mHolder.removeCallback(this);
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            } catch (Exception e) {
                Timber.d("Failed stopping camera preview", e.getMessage());
            }
        }
    }

    @Override
    public void onError(int error, Camera camera) {
        mCamera1Activity.restartCamera();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        if (sizes == null) return null;
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) height / width;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - height) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - height);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
        return optimalSize;
    }
}
