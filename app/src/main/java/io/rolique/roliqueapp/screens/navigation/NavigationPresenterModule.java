package io.rolique.roliqueapp.screens.navigation;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class NavigationPresenterModule {

    private final NavigationActivity mView;

    NavigationPresenterModule(NavigationActivity view) {
        mView = view;
    }

    @Provides
    @ViewScope
    NavigationActivity provideNavigationActivity() {
        return mView;
    }
}
