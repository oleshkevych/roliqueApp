package io.rolique.roliqueapp.screens.userCheckIns;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;
import io.rolique.roliqueapp.screens.profile.ProfileActivity;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = {RoliqueApplicationComponent.class}, modules = {UserCheckInsStatisticPresenterModule.class})
interface UserCheckInsStatisticComponent {

    void inject(UserCheckInsStatisticActivity checkInsStatisticActivity);
}
