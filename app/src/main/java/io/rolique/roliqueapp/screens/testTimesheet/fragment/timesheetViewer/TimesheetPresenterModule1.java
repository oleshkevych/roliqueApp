package io.rolique.roliqueapp.screens.testTimesheet.fragment.timesheetViewer;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class TimesheetPresenterModule1 {

    private final TimesheetViewerFragment mView;

    TimesheetPresenterModule1(TimesheetViewerFragment view) {
        mView = view;
    }

    @Provides
    @ViewScope
    TimesheetViewerFragment provideTimesheetFragment() {
        return mView;
    }
}
