package io.rolique.roliqueapp;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rolique.roliqueapp.screens.ViewScope;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Module
public class RoliqueApplicationModule {

    private final Context mContext;

    public RoliqueApplicationModule(Context context) {
        mContext = context;
    }

    @Provides
    Context provideContext() {
        return mContext;
    }

    @Provides
    @Singleton
    public RoliqueApplicationPreferences providePreferences() {
        return new RoliqueApplicationPreferences(mContext);
    }
}
