package io.rolique.roliqueapp;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Singleton
@Component(modules = {RoliqueApplicationModule.class})
public interface RoliqueApplicationComponent {

    RoliqueApplicationPreferences getApplicationPreferences();

    Context getAppContext();
}