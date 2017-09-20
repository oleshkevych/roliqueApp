package io.rolique.roliqueapp.util.ui;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Calendar;
import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;

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

}
