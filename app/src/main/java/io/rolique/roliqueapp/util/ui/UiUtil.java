package io.rolique.roliqueapp.util.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.FitCenter;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.glide.GlideApp;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static String getUserNameForView(String senderId, List<User> users) {
        for (User user : users)
            if (user.getId().equals(senderId))
                return getUserNameForView(user);
        return "unknown user";
    }

    public static String getUserNameForView(User user) {
        return String.format("%s %s", user.getFirstName(), user.getLastName());
    }

    public static void setImageWithRoundCorners(final ImageView imageView, String url) {
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        GlideApp.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(cornerRadius)))
                .placeholder(R.color.green_700_alpha_50)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        ViewCompat.setHasTransientState(imageView, true);
                        final ObservableColorMatrix cm = new ObservableColorMatrix();
                        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
                        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {

                                cm.setSaturation(animation.getAnimatedFraction());
                                if (imageView.getDrawable() != null) {
                                    imageView.getDrawable().setColorFilter(new ColorMatrixColorFilter(cm));
                                }
                            }

                        });
                        animation.setDuration(2000);
                        animation.addListener(new AnimatorListenerAdapter() {

                            public void onAnimationEnd(Animator animation) {
                                ViewCompat.setHasTransientState(imageView, false);
                            }
                        });
                        animation.start();
                        return false;
                    }
                })
                .into(imageView);
    }

    public static void setImageWithRoundTopCorners(ImageView imageView, String url) {
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        GlideApp.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(cornerRadius)))
                .placeholder(R.color.grey_300)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    public static void setImageWithRoundBottomCorners(ImageView imageView, String url) {
        int cornerRadius = imageView.getContext().getResources().getDimensionPixelSize(R.dimen.image_view_corner_radius);
        GlideApp.with(imageView.getContext())
                .load(url)
                .apply(new RequestOptions().transforms(new FitCenter(), new RoundedCorners(cornerRadius)))
                .placeholder(R.color.grey_300)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(imageView);
    }

    public static void updateImageIfExists(ViewSwitcher viewSwitcher, String imagePath, String name) {
        setImageIfExists(viewSwitcher, imagePath, name, 0);
    }

    public static void setImageIfExists(ViewSwitcher viewSwitcher, String imagePath, String name, int sizeInDp) {
        if (sizeInDp != 0) {
            int imageSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sizeInDp, viewSwitcher.getContext().getResources().getDisplayMetrics());
            viewSwitcher.getLayoutParams().height = imageSize;
            viewSwitcher.getLayoutParams().width = imageSize;
        }
        if (imagePath.isEmpty()) {
            viewSwitcher.setDisplayedChild(0);
            TextView textView = viewSwitcher.findViewById(R.id.text_view_image);
            String text = "";
            if (!name.isEmpty()) {
                if (name.trim().contains(" ")) {
                    String[] letters = name.split(" ");
                    int i = 0;
                    while (text.length() < 2 && letters.length > i) {
                        if (!letters[i].trim().isEmpty())
                            text += letters[i].trim().substring(0, 1)
                                    .toUpperCase();
                        i++;
                    }
                } else {
                    text += name.substring(0, 1)
                            .toUpperCase();
                }
            }
            if (sizeInDp != 0)
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeInDp / 2.5f);
            textView.setText(text);
            textView.setTextColor(getRandomColor(name.trim()));
        } else {
            viewSwitcher.setDisplayedChild(1);
            ImageView imageView = viewSwitcher.findViewById(R.id.image_view);
            setImage(imageView, imagePath);
        }
    }

    private static int getRandomColor(String name) {
        int wight = 0;
        for (Character character : name.toCharArray())
            wight += character;

        if (name.length() < 5)
            return Color.argb(255, 255 - (wight % 255), wight % 255, wight % 255);
        if (name.length() < 8)
            return Color.argb(255, wight % 255, 255 - (wight % 255), wight % 255);
        if (name.length() < 12)
            return Color.argb(255, wight % 255, wight % 255, 255 - (wight % 255));
        if (name.length() < 18)
            return Color.argb(255, 255 - (wight % 255), wight % 255, 255 - (wight % 255));
        return Color.argb(255, wight % 255, 255 - (wight % 255), 255 - (wight % 255));
    }

    private static void setImage(ImageView imageView, String path) {
        GlideApp.with(imageView.getContext())
                .load(path)
                .apply(new RequestOptions().transforms(new CircleCrop()))
                .into(imageView);
    }
}
