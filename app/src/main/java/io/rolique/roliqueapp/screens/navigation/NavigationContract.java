package io.rolique.roliqueapp.screens.navigation;

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
    }

    interface Presenter extends BasePresenter {
        void isLogin();
        void logout();
        void updateUserPicture(Media media);
    }
}
