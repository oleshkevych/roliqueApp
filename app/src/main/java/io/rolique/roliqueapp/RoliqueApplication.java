package io.rolique.roliqueapp;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RoliqueApplication extends MultiDexApplication {

    private RoliqueApplicationComponent mRepositoryComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

// Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(RoliqueApplication.this, crashlyticsKit);
        plantTimber();
        Timber.e("ID: " + FirebaseInstanceId.getInstance().getToken());
        FirebaseMessaging.getInstance().subscribeToTopic("main");
        mRepositoryComponent = buildRepositoryComponent();
    }

    private void plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }

    private RoliqueApplicationComponent buildRepositoryComponent() {
        return DaggerRoliqueApplicationComponent.builder()
                .roliqueApplicationModule(new RoliqueApplicationModule((getApplicationContext()),
                        FirebaseAuth.getInstance(),
                        FirebaseDatabase.getInstance()))
                .build();
    }

    public RoliqueApplicationComponent getRepositoryComponent() {
        return mRepositoryComponent;
    }
}
