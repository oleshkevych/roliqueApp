package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;
import android.view.KeyEvent;

/**
 * Created by Volodymyr Oleshkevych on 9/13/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class KeyboardEditText extends AppCompatEditText {

    public interface OnKeyboardChangeListener {
        void onKeyboardStateChanged(boolean isVisible);
    }

    private OnKeyboardChangeListener mOnKeyboardChangeListener;

    public KeyboardEditText(Context context) {
        super(context);
    }

    public KeyboardEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KeyboardEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setOnKeyboardChangeListener(OnKeyboardChangeListener onKeyboardChangeListener) {
        mOnKeyboardChangeListener = onKeyboardChangeListener;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        super.setOnTouchListener(l);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) setCursorVisible(true);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, @NonNull KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnKeyboardChangeListener != null)
                mOnKeyboardChangeListener.onKeyboardStateChanged(false);

            setCursorVisible(false);
        }
        return super.onKeyPreIme(keyCode, event);
    }
}