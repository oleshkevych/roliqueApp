package io.rolique.roliqueapp.screens.timesheetViewer;

import java.util.Date;
import java.util.List;

import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface TimesheetContract {

    interface View extends BaseView<Presenter> {
        void updateTable(List<User> users);
        void showProgressInView(boolean isActive);
    }

    interface Presenter extends BasePresenter {
        void fetchTimesheetsByDate(Date date);
    }
}
