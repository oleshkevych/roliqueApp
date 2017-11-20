package io.rolique.roliqueapp.screens.userCheckIns;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class UserCheckInsStatisticPresenter implements UserCheckInsStatisticContract.Presenter, FirebaseValues {

    private final UserCheckInsStatisticContract.View mView;
    FirebaseDatabase mDatabase;

    @Inject
    UserCheckInsStatisticPresenter(UserCheckInsStatisticContract.View view,
                                   FirebaseDatabase database) {
        mView = view;
        mDatabase = database;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void getTimesheetByTime(final User user) {
        mView.showProgressInView(true);
        DatabaseReference mTimesheetReference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN));
        mTimesheetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CheckIn> checkIns = parseCheckIns(dataSnapshot);

                setCheckInsToUsers(checkIns, user);
                updateCheckInsInView(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @NonNull
    List<CheckIn> parseCheckIns(DataSnapshot dataSnapshot) {
        List<CheckIn> checkIns = new ArrayList<>();
        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren())
            for (DataSnapshot snapshot : dataSnapshot1.getChildren()) {
                CheckIn checkIn = snapshot.getValue(CheckIn.class);
                assert checkIn != null;
                checkIn.setUserId(snapshot.getKey());
                checkIns.add(checkIn);
            }
        return checkIns;
    }

    void setCheckInsToUsers(List<CheckIn> checkIns, User user) {
        for (CheckIn checkIn : checkIns)
            if (user.getId().equals(checkIn.getUserId()))
                user.addCheckIn(checkIn);
    }

    void updateCheckInsInView(User user) {
        mView.showCheckInInView(user);
        mView.showProgressInView(false);
    }
}
