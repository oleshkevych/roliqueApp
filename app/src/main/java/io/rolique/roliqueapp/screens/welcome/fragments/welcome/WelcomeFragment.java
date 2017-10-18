package io.rolique.roliqueapp.screens.welcome.fragments.welcome;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.welcome.WelcomeActivity;
import io.rolique.roliqueapp.screens.welcome.fragments.signIn.SignInFragment;
import io.rolique.roliqueapp.screens.welcome.fragments.signUp.SignUpFragment;

/**
 * Created by Volodymyr Oleshkevych on 8/24/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class WelcomeFragment extends BaseFragment {

    public static Fragment newInstance() {
        return new WelcomeFragment();
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_welcome, container, false);
    }

    @Override
    protected void inject() {

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpToolbar();
    }

    private void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_welcome_title);
    }

    @OnClick(R.id.button_sign_in)
    void onSignInClick() {
        getFragmentManager().
                beginTransaction().
                replace(R.id.fragments_container, SignInFragment.newInstance()).
                commit();
    }

    @OnClick(R.id.button_sign_up)
    void onSignUpClick() {
        getFragmentManager().
                beginTransaction().
                replace(R.id.fragments_container, SignUpFragment.newInstance(), WelcomeActivity.SIGN_UP_FRAGMENT_TAG).
                commit();

    }
}
