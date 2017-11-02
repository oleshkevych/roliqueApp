package io.rolique.roliqueapp.screens.timesheetViewer;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class TimesheetPresenterModule {

    private final TimesheetViewerActivity mView;

    TimesheetPresenterModule(TimesheetViewerActivity view) {
        mView = view;
    }

    @Provides
    @ViewScope
    TimesheetViewerActivity provideTimesheetActivity() {
        return mView;
    }
}
