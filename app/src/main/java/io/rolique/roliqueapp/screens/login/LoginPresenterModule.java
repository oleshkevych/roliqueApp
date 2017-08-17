package io.rolique.roliqueapp.screens.login;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class LoginPresenterModule {

    private final LoginActivity mView;

    LoginPresenterModule(LoginActivity view) {
        mView = view;
    }

    @Provides
    @ViewScope
    LoginActivity provideLoginContractView() {
        return mView;
    }
}
