package io.rolique.roliqueapp.screens.welcome.fragments.signUp;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class SignUpPresenterModule {

    private final SignUpFragment mView;

    SignUpPresenterModule(SignUpFragment view) {
        mView = view;
    }

    @Provides
    @ViewScope
    SignUpFragment provideSignUpContractView() {
        return mView;
    }
}
