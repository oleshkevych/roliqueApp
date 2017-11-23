package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ScrollView;

import io.rolique.roliqueapp.R;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/23/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ScrollViewWithMaxHeight extends ScrollView {

    public static int WITHOUT_MAX_HEIGHT_VALUE = -1;

    private int mMaxHeight = WITHOUT_MAX_HEIGHT_VALUE;

    public ScrollViewWithMaxHeight(Context context) {
        super(context);
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScrollViewWithMaxHeight,
                0, 0);

        try {
            setMaxHeight(a.getDimensionPixelSize(R.styleable.ScrollViewWithMaxHeight_maxHeight, 0));

        } finally {
            a.recycle();
        }
    }

    public ScrollViewWithMaxHeight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            if (mMaxHeight != WITHOUT_MAX_HEIGHT_VALUE
                    && heightSize > mMaxHeight) {
                heightSize = mMaxHeight;
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.AT_MOST);
            getLayoutParams().height = heightSize;
        } catch (Exception e) {
            Timber.e("onMesure", "Error forcing height: " + e);
        } finally {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
    }
}