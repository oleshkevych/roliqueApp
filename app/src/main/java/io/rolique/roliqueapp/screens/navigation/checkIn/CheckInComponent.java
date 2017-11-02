package io.rolique.roliqueapp.screens.navigation.checkIn;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;
import io.rolique.roliqueapp.screens.timesheetViewer.TimesheetViewerActivity;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = {RoliqueApplicationComponent.class}, modules = {CheckInPresenterModule.class})
interface CheckInComponent {

    void inject(CheckInFragment checkInFragment);
}
