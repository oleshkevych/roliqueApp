package io.rolique.cameralibrary.uiUtil;

import android.net.Uri;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.rolique.cameralibrary.R;
import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static void setImageWithRoundCorners(final ImageView imageView, File file) {
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        Picasso picasso = new Picasso.Builder(imageView.getContext()).listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                exception.printStackTrace();
            }
        }).build();
        picasso
                .load(file)
                .fit()
                .centerCrop()
                .transform(new RoundedCornersTransformation(cornerRadius, 0))
                .placeholder(R.color.grey_300)
                .into(imageView);
    }
}
