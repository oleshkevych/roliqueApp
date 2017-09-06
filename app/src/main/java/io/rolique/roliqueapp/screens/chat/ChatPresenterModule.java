package io.rolique.roliqueapp.screens.chat;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class ChatPresenterModule {

    private final ChatContract.View mView;

    ChatPresenterModule(ChatContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    ChatContract.View provideChatView() {
        return mView;
    }
}
