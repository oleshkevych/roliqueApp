package io.rolique.roliqueapp.screens.timesheetViewer;

import android.support.annotation.NonNull;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class TimesheetPresenter implements TimesheetContract.Presenter, FirebaseValues {

    private final TimesheetViewerActivity mView;

    RoliqueApplicationPreferences mPreferences;
    List<User> mUsers;
    FirebaseDatabase mDatabase;
    DatabaseReference mTimesheetReference;
    ChildEventListener mChildEventListener;

    @Inject
    TimesheetPresenter(RoliqueApplicationPreferences preferences,
                       TimesheetViewerActivity view,
                       RoliqueAppUsers users,
                       FirebaseDatabase database) {
        mView = view;
        mPreferences = preferences;
        mUsers = new ArrayList<>(users.getUsers());
        mDatabase = database;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        resetListener();
    }

    @Override
    public void fetchTimesheetsByDate(Date date) {
        mView.showProgressInView(true);
        setTimesheetListenerByTime(date);
    }


    private void setTimesheetListenerByTime(Date date) {
        resetListener();

        mTimesheetReference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN));
//        mChildEventListener = new ChildEventListener() {
//
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                List<CheckIn> checkIns = parseCheckIns(dataSnapshot);
//
//                setCheckInsToUsers(checkIns);
//                updateCheckInsInView();
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                List<CheckIn> checkIns = parseCheckIns(dataSnapshot);
//
//                setCheckInsToUsers(checkIns);
//                updateCheckInsInView();
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//        mTimesheetReference.addChildEventListener(mChildEventListener);
        mTimesheetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CheckIn> checkIns = parseCheckIns(dataSnapshot);

                setCheckInsToUsers(checkIns);
                updateCheckInsInView();
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

    void setCheckInsToUsers(List<CheckIn> checkIns) {
        for (CheckIn checkIn : checkIns)
            for (User user : mUsers)
                if (user.getId().equals(checkIn.getUserId())) {
                    user.addCheckIn(checkIn);
                    break;
                }
    }

    void updateCheckInsInView() {
        mView.updateTable(mUsers);
        mView.showProgressInView(false);
    }

    private void resetListener() {
        if (mTimesheetReference != null) {
            mTimesheetReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
            mTimesheetReference = null;
        }
    }
}
