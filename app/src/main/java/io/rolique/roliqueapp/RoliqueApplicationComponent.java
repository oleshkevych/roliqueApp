package io.rolique.roliqueapp;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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

    FirebaseDatabase getFirebaseDatabase();

    FirebaseAuth getFirebaseAuth();

    RoliqueAppUsers getRoliqueAppUsers();

}