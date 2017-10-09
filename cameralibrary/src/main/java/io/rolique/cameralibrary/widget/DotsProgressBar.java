package io.rolique.cameralibrary.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import io.rolique.cameralibrary.R;

/**
 * Created by Volodymyr Oleshkevych on 10/9/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class DotsProgressBar extends View {
    private float mRadius;

    private Paint mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Handler mHandler = new Handler();
    private int mIndex = 0;
    private int widthSize, heightSize;
    private int mDotCount = 6;
    List<Paint> mPaintList = new ArrayList<>();

    public DotsProgressBar(Context context) {
        super(context);
        init(context);
    }

    public DotsProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DotsProgressBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mRadius = context.getResources().getDimension(R.dimen.circle_indicator_radius);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(Color.WHITE);
        int mPrevColor = 0;
        for (int i = 0; i < mDotCount; i++) {
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.FILL);
            mPrevColor = getRandomColor(mPrevColor);
            paint.setColor(mPrevColor);
            mPaintList.add(paint);
        }
        start();
    }

    public void setDotsCount(int count) {
        mDotCount = count;
    }

    public void start() {
        mIndex = -1;
        mHandler.removeCallbacks(mRunnable);
        mHandler.post(mRunnable);
    }

    public void stop() {
        mHandler.removeCallbacks(mRunnable);
    }

    private int step = 1;
    private Runnable mRunnable = new Runnable() {

        @Override
        public void run() {
            mIndex += step;
            if (mIndex < 0) {
                mIndex = 1;
                step = 1;
            } else if (mIndex > (mDotCount - 1)) {
                if ((mDotCount - 2) >= 0) {
                    mIndex = mDotCount - 2;
                    step = -1;
                } else{
                    mIndex = 0;
                    step = 1;
                }
            }
            invalidate();
            mHandler.postDelayed(mRunnable, 400);
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
        heightSize = (int) mRadius * 2 + getPaddingBottom() + getPaddingTop();
        setMeasuredDimension(widthSize, heightSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float margin = mRadius;
        float dX = (widthSize - mDotCount * mRadius * 2 - (mDotCount - 1) * margin) / 2.0f;
        float dY = heightSize / 2;
        for (int i = 0; i < mDotCount; i++) {
            if (i == mIndex) {
                canvas.drawCircle(dX, dY, mRadius, mPaintFill);
            } else {
                canvas.drawCircle(dX, dY, mRadius, mPaintList.get(i));
            }
            dX += (2 * mRadius + margin);
        }
    }

    private int getRandomColor(int prevColor) {
        int ran = (int) Math.round(Math.random() * 5);
        switch (ran) {
            case 1:
                return checkDuplicates(prevColor, Color.RED);
            case 2:
                return checkDuplicates(prevColor, Color.GREEN);
            case 3:
                return checkDuplicates(prevColor, Color.BLUE);
            default:
            case 5:
                return checkDuplicates(prevColor, Color.CYAN);
            case 6:
                return checkDuplicates(prevColor, Color.MAGENTA);
        }
    }

    private int checkDuplicates(int prevColor, int currentColor) {
        if (prevColor == currentColor) return getRandomColor(prevColor);
        else return currentColor;
    }
}