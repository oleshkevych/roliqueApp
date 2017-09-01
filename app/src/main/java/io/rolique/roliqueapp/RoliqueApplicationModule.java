package io.rolique.roliqueapp;

import android.content.Context;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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

    private final FirebaseAuth mFirebaseAuth;
    private final Context mContext;
    private final FirebaseDatabase mFirebaseDatabase;

    public RoliqueApplicationModule(Context context, FirebaseAuth auth, FirebaseDatabase database) {
        mContext = context;
        mFirebaseAuth = auth;
        mFirebaseDatabase = database;
    }

    @Provides
    @Singleton
    FirebaseDatabase provideDatabase() {
        return mFirebaseDatabase;
    }

    @Provides
    @Singleton
    FirebaseAuth provideAuth() {
        return mFirebaseAuth;
    }

    @Provides
    @Singleton
    public RoliqueApplicationPreferences providePreferences() {
        return new RoliqueApplicationPreferences(mContext);
    }

    @Provides
    @Singleton
    public RoliqueAppUsers provideUsers() {
        return new RoliqueAppUsers(mFirebaseAuth, mFirebaseDatabase);
    }
}
