package io.rolique.roliqueapp.screens.navigation.checkIn;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;
import io.rolique.roliqueapp.screens.timesheetViewer.TimesheetViewerActivity;

/**
 * Created by Volodymyr Oleshkevych on 11/2/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class CheckInPresenterModule {

    private final CheckInFragment mView;

    CheckInPresenterModule(CheckInFragment view) {
        mView = view;
    }

    @Provides
    @ViewScope
    CheckInFragment provideCheckInFragment() {
        return mView;
    }
}
