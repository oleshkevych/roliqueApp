package io.rolique.roliqueapp.screens.chat;

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
        void showTopMessagesView(List<Message> messages, boolean isFetchMessageEnabled);
        void showNewMessageView(Message message);
        void updateMessageView(Message message);
        void removedMessageView(String messageId);
        void showErrorInView(String message);
        void showLeaveInView();
    }

    interface Presenter extends BasePresenter {
        void getTopMessages(String firstMessageId, Chat chat);
        void fetchLastMessages(Chat chat);
        void leaveChat(Chat chat, String memberId);
        void setMessage(Message message, Chat chat);
        void removeMessage(Message message, Chat chat, boolean isInLast20th);
    }
}
