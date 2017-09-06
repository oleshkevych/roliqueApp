package io.rolique.roliqueapp.screens.chat;

import android.graphics.Bitmap;

import java.util.List;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ChatContract {

    interface View extends BaseView<Presenter> {
        void showLastMessagesView(List<Message> messages);
        void showTopMessagesView(List<Message> messages);
        void showNewMessageView(Message message);
        void setProgressIndicator(boolean active);
        void showErrorInView(String message);
    }

    interface Presenter extends BasePresenter {
        void getTopMessages(String firstMessageId, Chat chat);
        void fetchLastMessages(Chat chat);
        void addMessages(Message message, Chat chat);
    }
}
