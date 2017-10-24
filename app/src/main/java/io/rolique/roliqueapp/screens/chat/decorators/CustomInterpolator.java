package io.rolique.roliqueapp.screens.chat.decorators;

import android.view.animation.Interpolator;

/**
 * Created by Volodymyr Oleshkevych on 10/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class CustomInterpolator implements Interpolator {

    public CustomInterpolator() {
    }

    @Override
    public float getInterpolation(float t) {
        return 1f - (float) Math.pow((1 - t), 8);
    }
}