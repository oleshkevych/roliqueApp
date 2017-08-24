package io.rolique.roliqueapp.screens.welcome.fragments.signIn;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class SignInPresenterModule {

    private final SignInFragment mView;

    SignInPresenterModule(SignInFragment view) {
        mView = view;
    }

    @Provides
    @ViewScope
    SignInFragment provideSignInContractView() {
        return mView;
    }
}
