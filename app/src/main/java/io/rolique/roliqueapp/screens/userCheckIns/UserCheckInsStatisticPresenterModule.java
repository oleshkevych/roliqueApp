package io.rolique.roliqueapp.screens.userCheckIns;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class UserCheckInsStatisticPresenterModule {

    private final UserCheckInsStatisticContract.View mView;

    UserCheckInsStatisticPresenterModule(UserCheckInsStatisticContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    UserCheckInsStatisticContract.View provideProfileView() {
        return mView;
    }
}
