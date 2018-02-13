package io.rolique.roliqueapp.screens.navigation.checkIn;

import java.util.Date;
import java.util.List;

import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 11/2/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface CheckInContract {

    interface View extends BaseView<Presenter> {
        void updateCheckInInView(boolean isCheckedIn);
        void showCheckInInView(String checkInType);
        void showConnectionErrorInView();
        void updateAlarm(boolean isCheckedIn);
    }

    interface Presenter extends BasePresenter {
        void isUserAlreadyCheckedIn(Date date);
        void setNewCheckIn(CheckIn checkIn, Date date);
    }
}
