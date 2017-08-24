package io.rolique.roliqueapp.util.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static void setImage(ImageView image, String path) {
        Glide.with(image.getContext())
                .load(path)
                .placeholder(R.color.white)
                .crossFade()
                .into(image);
    }
}
