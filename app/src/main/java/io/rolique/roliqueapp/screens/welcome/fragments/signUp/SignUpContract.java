package io.rolique.roliqueapp.screens.welcome.fragments.signUp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

interface SignUpContract {

    interface View extends BaseView<Presenter> {
        void showLoginInView();
        void showLoginError(String message);
    }

    interface Presenter extends BasePresenter {
        void uploadImage(String imagePath, String email, String password, String firstName, String lastName, Activity activity);
    }
}
