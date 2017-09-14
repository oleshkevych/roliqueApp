package io.rolique.roliqueapp.screens.profileDetailes;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class ProfileDetailsPresenterModule {

    private final ProfileDetailsContract.View mView;

    ProfileDetailsPresenterModule(ProfileDetailsContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    ProfileDetailsContract.View provideProfileDetailsView() {
        return mView;
    }
}
