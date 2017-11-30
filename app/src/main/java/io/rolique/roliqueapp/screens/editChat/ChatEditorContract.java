package io.rolique.roliqueapp.screens.editChat;

import android.content.Context;

import java.util.List;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ChatEditorContract {

    interface View extends BaseView<Presenter> {
        void showSavedInView(Chat chat);
        void showUserInView(List<User> users);
        void setProgressIndicator(boolean active);
        void showErrorInView(String message);
        void subscribeMembersInView(Chat chat);
        void showFinishAsyncInView();
        void setUnMutedUsers(Chat chat, List<User> unMutedUsers);
    }

    interface Presenter extends BasePresenter {
        void saveNewChat(Chat chat);
        void editChat(Chat newChat, Chat oldChat);
        void deleteChat(Chat chat);
        void subscribeMembers(Context context, Chat chat, List<User> unMutedMemberUsers);
        void deleteChatSubscribtion(Context context, Chat chat);
        void fetchMutedUsers(Chat chat);
    }
}
