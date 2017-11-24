package io.rolique.roliqueapp.screens.testTimesheet;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import java.util.Date;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.testTimesheet.fragment.timesheetViewer.TimesheetViewerFragment;
import io.rolique.roliqueapp.screens.testTimesheet.fragment.userCheckIns.UserCheckInsStatisticFragment;

/**
 * Created by Volodymyr Oleshkevych on 11/23/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class FlippableTimesheetActivity extends Activity {

    public static Intent startIntent(Activity activity) {
        return new Intent(activity, FlippableTimesheetActivity.class);
    }

    TimesheetViewerFragment mTimesheetViewerFragment;
    boolean mShowingBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_timesheet);

        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, createTimesheetViewerFragment())
                    .commit();
        }
    }

    private TimesheetViewerFragment createTimesheetViewerFragment() {
        mTimesheetViewerFragment = TimesheetViewerFragment.startIntent();
        mTimesheetViewerFragment.setOnUserClickListener(new TimesheetViewerFragment.OnUserClickListener() {
            @Override
            public void onUserClick(User user, Date tableDate) {
                flipCard(user);
            }
        });
        return mTimesheetViewerFragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mTimesheetViewerFragment.setConfigOrientation(newConfig.orientation);
    }

    public void flipCard(User user) {
        if (mShowingBack) {
            mShowingBack = false;
            getFragmentManager().popBackStack();
            return;
        }
        mShowingBack = true;
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(
                        R.animator.card_flip_right_in,
                        R.animator.card_flip_right_out,
                        R.animator.card_flip_left_in,
                        R.animator.card_flip_left_out)
                .replace(R.id.container, UserCheckInsStatisticFragment.startIntent(user))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mShowingBack = false;
    }
}