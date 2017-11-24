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
    Date mTableDate = new Date();
    int mConfigOrientation = Configuration.ORIENTATION_PORTRAIT;
    User mSelectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_timesheet);

//        setUpFragments();
        if (savedInstanceState == null) {
            getFragmentManager()
                    .beginTransaction()
                    .add(R.id.container, createTimesheetViewerFragment())
                    .commit();
        }
    }

    private TimesheetViewerFragment createTimesheetViewerFragment() {
        mTimesheetViewerFragment = TimesheetViewerFragment.startIntent(mTableDate.getTime(), mConfigOrientation);
        mTimesheetViewerFragment.setOnUserClickListener(new TimesheetViewerFragment.OnUserClickListener() {
            @Override
            public void onUserClick(User user, Date tableDate) {
                mSelectedUser = user;
                mTableDate = tableDate;
                flipCard();
//                Bundle args = new Bundle();
//                args.putLong(TimesheetViewerFragment.ARG_DATE, mTableDate.getTime());
//                args.putInt(TimesheetViewerFragment.ARG_ORIENTATION, mConfigOrientation);
//                mTimesheetViewerFragment.setArguments(args);
            }
        });
        return mTimesheetViewerFragment;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigOrientation = newConfig.orientation;
//        Bundle args = new Bundle();
//        args.putLong(TimesheetViewerFragment.ARG_DATE, mTableDate.getTime());
//        args.putInt(TimesheetViewerFragment.ARG_ORIENTATION, mConfigOrientation);
//        mTimesheetViewerFragment.setArguments(args);
        mTimesheetViewerFragment.setConfigOrientation(mConfigOrientation);
    }

//    private void setUpFragments() {
//        mTimesheetViewerFragment = TimesheetViewerFragment.startIntent();
//        mUserCheckInsStatisticFragment = UserCheckInsStatisticFragment.startIntent();
//        mTimesheetViewerFragment.setOnUserClickListener(new TimesheetViewerFragment.OnUserClickListener() {
//            @Override
//            public void onUserClick(User user, Date tableDate) {
//                mUserCheckInsStatisticFragment.setUser(user);
//                mTableDate = tableDate;
//                flipCard();
//            }
//        });
//    }

    public void flipCard() {
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
                .replace(R.id.container, UserCheckInsStatisticFragment.startIntent(mSelectedUser))
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mShowingBack = false;
    }
}