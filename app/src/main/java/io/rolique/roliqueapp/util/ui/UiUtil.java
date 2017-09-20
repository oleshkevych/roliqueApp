package io.rolique.roliqueapp.util.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.glide.GlideApp;
import io.rolique.roliqueapp.glide.GlideRoliqueComponent;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static void setImage(ImageView imageView, String path) {
        Glide.with(imageView.getContext())
                .load(path)
                .apply(RequestOptions.circleCropTransform())
                .into(imageView);
    }

    public static String getUserNameForView(String senderId, List<User> users) {
        for (User user: users)
            if (user.getId().equals(senderId))
                return getUserNameForView(user);
        return "unknown user";
    }

    public static String getUserNameForView(User user) {
        return String.format("%s %s", user.getFirstName(), user.getLastName());
    }

    public static void setImageWithRoundCorners(ImageView imageView, String url) {
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        GlideApp.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(cornerRadius)))
                .placeholder(R.color.grey_300)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }
}
