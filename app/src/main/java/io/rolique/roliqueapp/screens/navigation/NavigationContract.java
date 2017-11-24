package io.rolique.roliqueapp.screens.navigation;

import java.util.Date;

import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface NavigationContract {

    interface View extends BaseView<Presenter> {
        void showLoginInView(boolean isLogin);
        void setImage(String path, String userName);
        void setUserName(String userName);
        void setImageProgress(boolean isActive);
        void showCheckInStatusInView(boolean isCheckedIn);
        void showCheckedInInView(String checkInType);
        void showConnectionErrorInView();
        void updateAlarm(boolean isCheckedIn, String checkInTime, boolean isNotificationAllowed);
    }

    interface Presenter extends BasePresenter {
        void isLogin();
        void logout();
        void updateUserPicture(Media media);
        void checkIfUserCheckedIn();
        void setNewCheckIn(CheckIn checkIn, Date date);
        void sendMessageLateToMainChat(String messageText);
    }
}
