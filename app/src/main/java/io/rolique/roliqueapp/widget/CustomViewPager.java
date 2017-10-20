package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Volodymyr Oleshkevych on 10/13/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class CustomViewPager extends ViewPager {

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Boolean mDisable = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDisable && super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDisable && super.onTouchEvent(event);
    }

    public void setScroll(Boolean disable) {
        mDisable = disable;
    }
}