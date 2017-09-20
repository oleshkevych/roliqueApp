package io.rolique.roliqueapp.screens.camera;

import dagger.Component;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(modules = CameraPresenterModule.class)
interface CameraComponent {
    void inject(CameraBaseActivity activity);
}
