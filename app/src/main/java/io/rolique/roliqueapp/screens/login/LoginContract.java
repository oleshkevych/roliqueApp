package io.rolique.roliqueapp.screens.login;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.File;

import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

interface LoginContract {

    interface View extends BaseView<Presenter> {

        void showLoginInView();
        void showLoginError();
    }

    interface Presenter extends BasePresenter {

        void uploadImage(@NonNull Bitmap bitmap, String email, String password, String firstName, String lastName);
        void signIn(String email, String password);
    }
}
