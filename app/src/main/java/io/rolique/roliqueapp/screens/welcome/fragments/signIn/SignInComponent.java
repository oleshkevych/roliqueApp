package io.rolique.roliqueapp.screens.welcome.fragments.signIn;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = RoliqueApplicationComponent.class, modules = {SignInPresenterModule.class})
interface SignInComponent {

    void inject(SignInFragment activity);
}
