package io.rolique.roliqueapp.screens.welcome.fragments.signIn;

import android.app.Activity;

import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

interface SignInContract {

    interface View extends BaseView<Presenter> {
        void showLoginInView();
        void showLoginError(String message);
    }

    interface Presenter extends BasePresenter {
        void signIn(String email, String password, Activity activity);
    }
}
