package io.rolique.roliqueapp;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RoliqueApplication extends Application {

    private RoliqueApplicationComponent mRepositoryComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        plantTimber();
        mRepositoryComponent = buildRepositoryComponent();
    }

    private void plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }
    private RoliqueApplicationComponent buildRepositoryComponent() {
        return DaggerRoliqueApplicationComponent.builder()
                .roliqueApplicationModule(new RoliqueApplicationModule((getApplicationContext())))
                .build();
    }

    public RoliqueApplicationComponent getRepositoryComponent() {
        return mRepositoryComponent;
    }
}
