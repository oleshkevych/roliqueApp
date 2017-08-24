package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 8/23/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class DrawableTextView extends AppCompatTextView {

    public DrawableTextView(Context context) {
        super(context);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    void initAttrs(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray attributeArray = context.obtainStyledAttributes(
                    attrs,
                    R.styleable.DrawableTextView);

            Drawable drawableLeft = null;
            Drawable drawableRight = null;
            Drawable drawableBottom = null;
            Drawable drawableTop = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                drawableLeft = attributeArray.getDrawable(R.styleable.DrawableTextView_drawableLeftCompat);
                drawableRight = attributeArray.getDrawable(R.styleable.DrawableTextView_drawableRightCompat);
                drawableBottom = attributeArray.getDrawable(R.styleable.DrawableTextView_drawableBottomCompat);
                drawableTop = attributeArray.getDrawable(R.styleable.DrawableTextView_drawableTopCompat);
            } else {
                final int drawableLeftId = attributeArray.getResourceId(R.styleable.DrawableTextView_drawableLeftCompat, -1);
                final int drawableRightId = attributeArray.getResourceId(R.styleable.DrawableTextView_drawableRightCompat, -1);
                final int drawableBottomId = attributeArray.getResourceId(R.styleable.DrawableTextView_drawableBottomCompat, -1);
                final int drawableTopId = attributeArray.getResourceId(R.styleable.DrawableTextView_drawableTopCompat, -1);

                if (drawableLeftId != -1)
                    drawableLeft = AppCompatResources.getDrawable(context, drawableLeftId);
                if (drawableRightId != -1)
                    drawableRight = AppCompatResources.getDrawable(context, drawableRightId);
                if (drawableBottomId != -1)
                    drawableBottom = AppCompatResources.getDrawable(context, drawableBottomId);
                if (drawableTopId != -1)
                    drawableTop = AppCompatResources.getDrawable(context, drawableTopId);
            }
            setCompoundDrawablesWithIntrinsicBounds(drawableLeft, drawableTop, drawableRight, drawableBottom);
            attributeArray.recycle();
        }
    }

    public void setDrawableRight(@DrawableRes int resId) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawableFromRes(resId), null);
    }

    public void setDrawableTop(@DrawableRes int resId) {
        setCompoundDrawablesWithIntrinsicBounds(null, getDrawableFromRes(resId), null, null);
    }

    private Drawable getDrawableFromRes(@DrawableRes int resId) {
        return AppCompatResources.getDrawable(getContext(), resId);
    }

    public void setTextColorResource(@ColorRes int colorRes) {
        setTextColor(ContextCompat.getColor(getContext(), colorRes));
    }
}