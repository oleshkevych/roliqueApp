package io.rolique.cameralibrary.screens.camera;

import dagger.Module;
import dagger.Provides;
import io.rolique.cameralibrary.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class CameraPresenterModule {

    private final CameraContract.View mView;

    CameraPresenterModule(CameraContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    CameraContract.View provideMainContractView() {
        return mView;
    }
}
