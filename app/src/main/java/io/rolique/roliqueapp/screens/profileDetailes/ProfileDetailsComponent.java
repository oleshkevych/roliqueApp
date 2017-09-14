package io.rolique.roliqueapp.screens.profileDetailes;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;
import io.rolique.roliqueapp.screens.editChat.ChatEditorActivity;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = {RoliqueApplicationComponent.class}, modules = {ProfileDetailsPresenterModule.class})
interface ProfileDetailsComponent {

    void inject(ProfileDetailsActivity profileDetailsActivity);
}
