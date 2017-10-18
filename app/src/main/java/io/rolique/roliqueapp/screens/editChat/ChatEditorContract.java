package io.rolique.roliqueapp.screens.editChat;

import android.graphics.Bitmap;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ChatEditorContract {

    interface View extends BaseView<Presenter> {
        void showSavedInView(Chat chat);
        void setProgressIndicator(boolean active);
        void showErrorInView(String message);
    }

    interface Presenter extends BasePresenter {
        void saveNewChat(Chat chat);
        void editChat(Chat newChat, Chat oldChat);
        void deleteChat(Chat chat);
    }
}
