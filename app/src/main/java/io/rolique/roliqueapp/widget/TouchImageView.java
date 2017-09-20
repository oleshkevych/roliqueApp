package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Volodymyr Oleshkevych on 5/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class TouchImageView extends ImageView {

    private static class Mode {
        static final int NONE = 0;
        static final int DRAG = 1;
        static final int ZOOM = 2;
    }

    private Matrix mMatrix;

    private int mMode;

    // Remember some things for zooming
    PointF mLastPoint = new PointF();
    PointF mStartPoint = new PointF();
    float mMinScale = 1f;
    float mMaxScale = 3f;
    float[] mValues;
    int mViewWidth;
    int mViewHeight;

    static final int CLICK = 3;

    float mSaveScale = 1f;

    protected float mOrigWidth, mOrigHeight;

    int oldMeasuredWidth, oldMeasuredHeight;

    ScaleGestureDetector mScaleDetector;

    Context mContext;

    public TouchImageView(Context context) {
        super(context);
        sharedConstructing(context);
    }

    public TouchImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedConstructing(context);
    }

    private void sharedConstructing(Context context) {

        super.setClickable(true);

        mContext = context;
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mMatrix = new Matrix();

        mValues = new float[9];

        setImageMatrix(mMatrix);
        setScaleType(ScaleType.MATRIX);

        setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                mScaleDetector.onTouchEvent(event);
                PointF curr = new PointF(event.getX(), event.getY());

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mLastPoint.set(curr);
                        mStartPoint.set(mLastPoint);
                        mMode = Mode.DRAG;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (mMode == Mode.DRAG) {
                            float deltaX = curr.x - mLastPoint.x;
                            float deltaY = curr.y - mLastPoint.y;

                            float fixTransX = getFixDragTrans(deltaX, mViewWidth, mOrigWidth * mSaveScale);
                            float fixTransY = getFixDragTrans(deltaY, mViewHeight, mOrigHeight * mSaveScale);

                            mMatrix.postTranslate(fixTransX, fixTransY);
                            fixTrans();

                            mLastPoint.set(curr.x, curr.y);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        mMode = Mode.NONE;
                        int xDiff = (int) Math.abs(curr.x - mStartPoint.x);
                        int yDiff = (int) Math.abs(curr.y - mStartPoint.y);
                        if (xDiff < CLICK && yDiff < CLICK)
                            performClick();
                        break;

                    case MotionEvent.ACTION_POINTER_UP:
                        mMode = Mode.NONE;
                        break;
                }

                setImageMatrix(mMatrix);
                invalidate();

                return true; // indicate event was handled
            }
        });
    }

    public void setMaxZoom(float x) {
        mMaxScale = x;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            mMode = Mode.ZOOM;
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            float scaleFactor = detector.getScaleFactor();

            float origScale = mSaveScale;
            mSaveScale *= scaleFactor;

            if (mSaveScale > mMaxScale) {
                mSaveScale = mMaxScale;
                scaleFactor = mMaxScale / origScale;
            } else if (mSaveScale < mMinScale) {
                mSaveScale = mMinScale;
                scaleFactor = mMinScale / origScale;
            }

            if (mOrigWidth * mSaveScale <= mViewWidth
                    || mOrigHeight * mSaveScale <= mViewHeight) {
                mMatrix.postScale(scaleFactor, scaleFactor, mViewWidth / 2, mViewHeight / 2);
            } else {
                mMatrix.postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
            }
            fixTrans();
            return true;
        }
    }

    void fixTrans() {
        mMatrix.getValues(mValues);
        float transX = mValues[Matrix.MTRANS_X];
        float transY = mValues[Matrix.MTRANS_Y];
        float fixTransX = getFixTrans(transX, mViewWidth, mOrigWidth * mSaveScale);
        float fixTransY = getFixTrans(transY, mViewHeight, mOrigHeight * mSaveScale);
        if (fixTransX != 0 || fixTransY != 0) {
            mMatrix.postTranslate(fixTransX, fixTransY);
        }
    }

    float getFixTrans(float trans, float viewSize, float contentSize) {

        float minTrans, maxTrans;
        if (contentSize <= viewSize) {
            minTrans = 0;
            maxTrans = viewSize - contentSize;
        } else {
            minTrans = viewSize - contentSize;
            maxTrans = 0;
        }

        if (trans < minTrans) {
            return -trans + minTrans;
        }

        if (trans > maxTrans) {
            return -trans + maxTrans;
        }
        return 0;
    }

    float getFixDragTrans(float delta, float viewSize, float contentSize) {

        if (contentSize <= viewSize) {
            return 0;
        }
        return delta;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mViewWidth = MeasureSpec.getSize(widthMeasureSpec);
        mViewHeight = MeasureSpec.getSize(heightMeasureSpec);

        //
        // Rescales mImage on rotation
        //
        if (oldMeasuredHeight == mViewWidth
                && oldMeasuredHeight == mViewHeight
                || mViewWidth == 0 || mViewHeight == 0) {
            return;
        }

        oldMeasuredHeight = mViewHeight;
        oldMeasuredWidth = mViewWidth;

        if (mSaveScale == 1f) {
            float scale;
            Drawable drawable = getDrawable();

            if (drawable == null
                    || drawable.getIntrinsicWidth() == 0
                    || drawable.getIntrinsicHeight() == 0) {
                return;
            }

            int bmWidth = drawable.getIntrinsicWidth();
            int bmHeight = drawable.getIntrinsicHeight();

            float scaleX = (float) mViewWidth / (float) bmWidth;
            float scaleY = (float) mViewHeight / (float) bmHeight;

            scale = Math.min(scaleX, scaleY);

            mMatrix.setScale(scale, scale);

            // Center the mImage

            float redundantYSpace = (float) mViewHeight - (scale * (float) bmHeight);
            float redundantXSpace = (float) mViewWidth - (scale * (float) bmWidth);

            redundantYSpace /= (float) 2;
            redundantXSpace /= (float) 2;

            mMatrix.postTranslate(redundantXSpace, redundantYSpace);

            mOrigWidth = mViewWidth - 2 * redundantXSpace;
            mOrigHeight = mViewHeight - 2 * redundantYSpace;

            setImageMatrix(mMatrix);
        }

        fixTrans();
    }
}