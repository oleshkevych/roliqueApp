package io.rolique.roliqueapp.screens.navigation.chat;

import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ChatsContract {

    interface View extends BaseView<Presenter> {
        void showLoginInView(boolean isLogin);
        void setImage(String path);
    }

    interface Presenter extends BasePresenter {
        void isLogin();
        void logout();
    }
}
