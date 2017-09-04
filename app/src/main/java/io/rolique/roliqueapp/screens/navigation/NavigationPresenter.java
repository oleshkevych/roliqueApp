package io.rolique.roliqueapp.screens.navigation;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class NavigationPresenter implements NavigationContract.Presenter, FirebaseValues {

    private final NavigationActivity mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseAuth mAuth;

    @Inject
    NavigationPresenter(RoliqueApplicationPreferences preferences, NavigationActivity view, FirebaseAuth auth) {
        mView = view;
        mPreferences = preferences;
        mAuth = auth;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void isLogin() {
        if (mAuth.getCurrentUser() != null && mPreferences.isLoggedIn()) {
            mView.showLoginInView(true);
            mView.setImage(mPreferences.getImageUrl());
            return;
        }
        mView.showLoginInView(false);
    }

    @Override
    public void logout() {
        mAuth.signOut();
        mPreferences.logOut();
        isLogin();
    }
}
