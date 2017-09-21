package io.rolique.roliqueapp.util.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
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
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.glide.GlideApp;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UiUtil {

    public static void setImage(ImageView imageView, String path) {
        GlideApp.with(imageView.getContext())
                .load(path)
                .apply(new RequestOptions().transforms(new CircleCrop()))
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
                        final ObservableColorMatrix cm  = new ObservableColorMatrix();
                        ValueAnimator animation = ValueAnimator.ofFloat(0f, 1f);
                        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {

                                cm.setSaturation(animation.getAnimatedFraction());
                                if(imageView.getDrawable() != null){
                                    imageView.getDrawable().setColorFilter(new ColorMatrixColorFilter(cm));
                                }
                            }

                        });
                        animation.setDuration(2000);
                        animation.addListener(new AnimatorListenerAdapter() {

                            public void onAnimationEnd(Animator animation) {
                                ViewCompat.setHasTransientState(imageView, false);
                            };

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
}
