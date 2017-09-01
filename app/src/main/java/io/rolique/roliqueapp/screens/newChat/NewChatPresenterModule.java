package io.rolique.roliqueapp.screens.newChat;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class NewChatPresenterModule {

    private final NewChatContract.View mView;

    NewChatPresenterModule(NewChatContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    NewChatContract.View provideNewChatView() {
        return mView;
    }
}
