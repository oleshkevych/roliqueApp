package io.rolique.roliqueapp.screens.navigation.checkIn;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 11/2/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class CheckInPresenter implements CheckInContract.Presenter, FirebaseValues {

    private final CheckInFragment mView;

    RoliqueApplicationPreferences mPreferences;
    List<User> mUsers;
    FirebaseDatabase mDatabase;
    DatabaseReference mTimesheetReference;
    final ConnectivityManager mConnectivityManager;

    @Inject
    CheckInPresenter(RoliqueApplicationPreferences preferences,
                     CheckInFragment view,
                     RoliqueAppUsers users,
                     FirebaseDatabase database,
                     ConnectivityManager connectivityManager) {
        mView = view;
        mPreferences = preferences;
        mUsers = new ArrayList<>(users.getUsers());
        mDatabase = database;
        mConnectivityManager = connectivityManager;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void isUserAlreadyCheckedIn(Date date) {
        if (lackInternetConnection()) {
            mView.showConnectionErrorInView();
            mView.updateCheckInInView(true);
            return;
        }
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        mTimesheetReference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN, mDateFormat.format(date)));
        mTimesheetReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isCheckedIn = false;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot1.getKey().equals(mPreferences.getId())) {
                        isCheckedIn = true;
                        break;
                    }
                }
                mView.updateCheckInInView(isCheckedIn);
                mView.updateAlarm(isCheckedIn, mPreferences.getNotificationTime(), mPreferences.isNotificationAllowed());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mView.updateCheckInInView(true);
            }
        });
    }

    @Override
    public void setNewCheckIn(CheckIn checkIn, Date date) {
        if (lackInternetConnection()) {
            mView.showConnectionErrorInView();
            return;
        }
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        DatabaseReference reference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN, mDateFormat.format(date), mPreferences.getId()));
        reference.setValue(checkIn);
        mView.showCheckInInView(checkIn.getType());
        mView.updateAlarm(true, mPreferences.getNotificationTime(), mPreferences.isNotificationAllowed());
    }

    private boolean lackInternetConnection() {
        boolean isConnected;

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo == null || !networkInfo.isConnectedOrConnecting());

        return isConnected;
    }
}
