package io.rolique.cameralibrary.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;

import java.lang.ref.WeakReference;

import io.rolique.cameralibrary.R;

/**
 * Created by Volodymyr Oleshkevych on 10/9/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RecordingCounterTextView extends DrawableTextView {

    public RecordingCounterTextView(Context context) {
        super(context);
    }

    public RecordingCounterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RecordingCounterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    AnimationHandler mAnimationHandler = new AnimationHandler(RecordingCounterTextView.this);

    public void start() {
        update(0);
    }

    public void stop() {
        mAnimationHandler.removeCallbacksAndMessages(null);
    }

    private void update(int calls) {
        int secs = calls / 2;
        String visibleSec = secs % 60 < 10 ? String.format("%s%s", 0, secs % 60) : String.valueOf(secs % 60);
        String mins = secs / 60 < 10 ? String.format("%s%s", 0, secs / 60) : String.valueOf(secs / 60);
        setText(String.format("%s:%s", mins, visibleSec));
        setDrawableLeft(calls % 2 == 0 ? R.drawable.ic_dot_transparent_24dp : R.drawable.ic_dot_red_24dp);
        Message message = new Message();
        message.obj = calls + 1;
        mAnimationHandler.sendMessageDelayed(message, 500);
    }

    private class AnimationHandler extends Handler {

        private WeakReference<RecordingCounterTextView> mReference;

        AnimationHandler(RecordingCounterTextView view) {
            mReference = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            RecordingCounterTextView view = mReference.get();
            view.update((Integer) msg.obj);
        }
    }
}
