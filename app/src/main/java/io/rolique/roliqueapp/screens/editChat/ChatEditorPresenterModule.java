package io.rolique.roliqueapp.screens.editChat;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
class ChatEditorPresenterModule {

    private final ChatEditorContract.View mView;

    ChatEditorPresenterModule(ChatEditorContract.View view) {
        mView = view;
    }

    @Provides
    @ViewScope
    ChatEditorContract.View provideChatEditorView() {
        return mView;
    }
}
