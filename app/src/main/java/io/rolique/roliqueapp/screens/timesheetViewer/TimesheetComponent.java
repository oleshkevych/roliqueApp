package io.rolique.roliqueapp.screens.timesheetViewer;

import dagger.Component;
import io.rolique.roliqueapp.RoliqueApplicationComponent;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@ViewScope
@Component(dependencies = {RoliqueApplicationComponent.class}, modules = {TimesheetPresenterModule.class})
interface TimesheetComponent {

    void inject(TimesheetViewerActivity navigationActivity);
}
