package io.rolique.roliqueapp.screens.login;

import dagger.Component;
import dagger.Provides;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = RoliqueApplicationComponent.class, modules = {LoginPresenterModule.class})
interface LoginComponent {

    void inject(LoginActivity activity);
}
