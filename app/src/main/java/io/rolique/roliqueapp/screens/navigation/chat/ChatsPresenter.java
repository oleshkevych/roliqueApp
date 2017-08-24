package io.rolique.roliqueapp.screens.navigation.chat;

import com.google.firebase.auth.FirebaseAuth;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.BasePresenter;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatsPresenter implements ChatsContract.Presenter {

    private final ChatsContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseAuth mAuth;

    @Inject
    ChatsPresenter(RoliqueApplicationPreferences preferences, ChatsContract.View view) {
        mView = view;
        mPreferences = preferences;
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void isLogin() {
        if(mAuth.getCurrentUser() != null) {
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
