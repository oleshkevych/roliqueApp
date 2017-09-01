package io.rolique.roliqueapp.screens.navigation.chat;

import java.util.List;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ChatsContract {

    interface View extends BaseView<Presenter> {
        void showAddedChatInView(Chat chat);
        void showChangedChatInView(Chat chat);
        void showRemovedChatInView(Chat chat);
        void showErrorInView(String message);
    }

    interface Presenter extends BasePresenter {
        void setUpChatsListener();
    }
}
