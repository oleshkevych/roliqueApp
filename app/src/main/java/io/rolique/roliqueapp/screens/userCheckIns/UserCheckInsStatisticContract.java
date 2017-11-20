package io.rolique.roliqueapp.screens.userCheckIns;

import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface UserCheckInsStatisticContract {

    interface View extends BaseView<Presenter> {
        void showCheckInInView(User user);
        void showProgressInView(boolean isActive);
    }

    interface Presenter extends BasePresenter {
        void getTimesheetByTime(User user);
    }
}
