package io.rolique.roliqueapp.util;

import android.content.Context;

import io.rolique.cameralibrary.screens.camera.CameraBaseActivity;

/**
 * Created by Volodymyr Oleshkevych on 9/21/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class CameraUtil {

    public static void startCamera(Context context) {
        context.startActivity(CameraBaseActivity.getStartIntent(context));
    }
}
