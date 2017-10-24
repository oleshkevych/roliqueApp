package io.rolique.roliqueapp.screens.chat.decorators;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.TranslateAnimation;

import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 10/23/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ItemDecorator extends RecyclerView.ItemDecoration {

    int mSpeed = 1;

    public void setSpeed(int dy) {
        this.mSpeed = dy;
    }

    public ItemDecorator() {
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int minOffset = 40;

        mSpeed = Math.abs(mSpeed) > minOffset ? mSpeed : mSpeed > 0 ? minOffset : -minOffset;
        int dy = mSpeed;
        if (parent.getChildAdapterPosition(view) > (parent.getChildCount() - 8) || parent.getChildAdapterPosition(view) < 8)
            dy = dy / 2;
        if (parent.getChildAdapterPosition(view) > (parent.getChildCount() - 5) || parent.getChildAdapterPosition(view) < 5)
            dy = dy / 3;
        if (parent.getChildAdapterPosition(view) > (parent.getChildCount() - 3) || parent.getChildAdapterPosition(view) < 3)
            dy = dy / 3;
        TranslateAnimation animation = new TranslateAnimation(0, 0, dy, 0);
        animation.setFillAfter(true);
        animation.setDuration(350);
        animation.setRepeatCount(0);
        view.startAnimation(animation);
    }
}