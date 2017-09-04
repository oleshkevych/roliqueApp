package io.rolique.roliqueapp.screens.navigation.chats;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class ChatsPresenterModule {

    private final ChatsContract.View mView;

    ChatsPresenterModule(ChatsContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    ChatsContract.View provideChatsView() {
        return mView;
    }
}
