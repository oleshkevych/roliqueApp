package io.rolique.roliqueapp.screens.profile;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class ProfilePresenterModule {

    private final ProfileContract.View mView;

    ProfilePresenterModule(ProfileContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    ProfileContract.View provideProfileView() {
        return mView;
    }
}
