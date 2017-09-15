package io.rolique.roliqueapp.screens.profile;

import android.util.Pair;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ProfilePresenter implements ProfileContract.Presenter, FirebaseValues {

    private final ProfileContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseDatabase mDatabase;
    DatabaseReference mUserDataReference;
    ChildEventListener mChildEventListener;

    @Inject
    ProfilePresenter(RoliqueApplicationPreferences preferences,
                     ProfileContract.View view,
                     FirebaseDatabase database) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
    }

    @Override
    public void start() {
        if (mUserDataReference != null)
            mUserDataReference.addChildEventListener(mChildEventListener);
    }

    @Override
    public void stop() {
        if (mUserDataReference != null)
            mUserDataReference.removeEventListener(mChildEventListener);
    }

    @Override
    public void getUserData(User user) {
        DatabaseReference dataRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH, USER_DATA, user.getId()));
        mChildEventListener = dataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                List<Pair<String, String>> pairs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    pairs.add(new Pair<>(postSnapshot.getKey(), postSnapshot.getValue(String.class)));
                mView.showValuesInView(dataSnapshot.getKey(), pairs);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                List<Pair<String, String>> pairs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    pairs.add(new Pair<>(postSnapshot.getKey(), postSnapshot.getValue(String.class)));
                mView.showValuesInView(dataSnapshot.getKey(), pairs);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mView.showRemoveCategoryInView(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void updateUser(User user) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USERS,
                        user.getId()));
        dataRef.setValue(user);
    }

    @Override
    public void setNewValue(User user, String category, String key, String value) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category,
                        key));
        dataRef.setValue(value);
    }

    @Override
    public void removeValue(User user, String category, String key) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category,
                        key));
        dataRef.removeValue();
    }

    @Override
    public void removeCategory(User user, String category) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category));
        dataRef.removeValue();
    }
}
