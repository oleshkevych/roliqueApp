package io.rolique.roliqueapp.screens.testTimesheet.fragment.timesheetViewer;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.BaseFragment;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.testTimesheet.fragment.timesheetViewer.adapters.SampleTableAdapter;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.widget.fixedHeaderTable.TableFixHeaders;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 10/31/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class TimesheetViewerFragment extends BaseFragment implements TimesheetContract1.View {

    public static final String ARG_DATE = "DATE";
    public static final String ARG_ORIENTATION = "ORIENTATION";

    public static TimesheetViewerFragment startIntent(long tableDate, int orientation) {
        Bundle args = new Bundle();
        args.putLong(ARG_DATE, tableDate);
        args.putInt(ARG_ORIENTATION, orientation);
        TimesheetViewerFragment fragment = new TimesheetViewerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String TABLE_DATE = "DATE";

    @Inject TimesheetPresenter1 mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.view_switcher) ViewSwitcher mViewSwitcher;
    @BindView(R.id.text_view_table_title) TextView mTitleTextView;
    @BindView(R.id.image_button_help) ImageButton mHelpButton;

    SampleTableAdapter mAdapter;
    Date mTableDate;
    private PopupWindow mPopupWindow;
    boolean mIsPopUpShowing;
    boolean mIsUserUpdated;
    int mConfigOrientation;

    public interface OnUserClickListener {
        void onUserClick(User user, Date tableDate);
    }

    OnUserClickListener mOnUserClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mTableDate == null && savedInstanceState == null) {
            mTableDate = new Date(getArguments().getLong(ARG_DATE));
            mConfigOrientation = getArguments().getInt(ARG_ORIENTATION);
        }
        return inflater.inflate(R.layout.activity_timesheet_viewer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState == null)
            updateViewOrientation();
        else
            mTableDate = new Date(savedInstanceState.getLong(TABLE_DATE));
        setUpToolbar(view);
        updateTimeInView(view);
        setUpTableView(view);
        if (!mIsUserUpdated)
            mPresenter.fetchTimesheetsByDate(new Date());
        setUpPopUpView();
    }

    protected void inject() {
        DaggerTimesheetComponent1.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getActivity().getApplication()).getRepositoryComponent())
                .timesheetPresenterModule1(new TimesheetPresenterModule1(TimesheetViewerFragment.this))
                .build()
                .inject(TimesheetViewerFragment.this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mConfigOrientation = newConfig.orientation;
        updateViewOrientation();
    }

    protected void updateViewOrientation() {
        if (mConfigOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            Timber.d("Landscape");
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mTitleTextView.setVisibility(View.GONE);
            mToolbar.setVisibility(View.GONE);

        } else if (mConfigOrientation == Configuration.ORIENTATION_PORTRAIT) {
            Timber.d("Portrait");
            mTitleTextView.setVisibility(View.VISIBLE);
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    public void setOnUserClickListener(OnUserClickListener onClickListener) {
        mOnUserClickListener = onClickListener;
    }

    public void setConfigOrientation(int configOrientation) {
        mConfigOrientation = configOrientation;
    }

    private void setUpToolbar(View view) {
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_timesheet_viewer_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
    }

    private void updateTimeInView(View view) {
        ((TextView) view.findViewById(R.id.text_view_table_date)).setText(getTableTime());
    }

    private String getTableTime() {
        if (DateUtil.isSameDay(mTableDate, new Date()))
            return "This week";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mTableDate);
        int currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        calendar.setTime(new Date(mTableDate.getTime() - (currentDayOfWeek - 1) * 24 * 60 * 60 * 1000));
        String dayStart = DateUtil.getStringDate(calendar.get(Calendar.DAY_OF_MONTH));
        String monthStart = DateUtil.getStringDate(calendar.get(Calendar.MONTH) + 1);
        calendar.setTime(new Date(mTableDate.getTime() + (7 - currentDayOfWeek) * 24 * 60 * 60 * 1000));
        String dayEnd = DateUtil.getStringDate(calendar.get(Calendar.DAY_OF_MONTH));
        String monthEnd = DateUtil.getStringDate(calendar.get(Calendar.MONTH) + 1);
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        return String.format("%s.%s - %s.%s.%s", dayStart, monthStart, dayEnd, monthEnd, year);
    }

    private void setUpTableView(View view) {
        TableFixHeaders tableFixHeaders = view.findViewById(R.id.table);
        tableFixHeaders.setRowSelectable(false);
        mAdapter = new SampleTableAdapter(getActivity(), mRoliqueAppUsers.getUsers(), mOnClickListener);
        tableFixHeaders.setAdapter(mAdapter);
    }

    SampleTableAdapter.OnClickListener mOnClickListener = new SampleTableAdapter.OnClickListener() {
        @Override
        public void onColumnClick(User user) {
            if (mOnUserClickListener == null) return;
            mOnUserClickListener.onUserClick(user, mTableDate);
        }
    };

    private void setUpPopUpView() {
        @SuppressLint("InflateParams") final View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.content_timesheed_help_popup, null);
        mPopupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.shape_text_view_message_alien_single));
        mPopupWindow.setOutsideTouchable(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mPopupWindow.setAttachedInDecor(true);
        }
        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.setAnimationStyle(R.style.popupTimesheetAnimation);
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mIsPopUpShowing = false;
                        mHelpButton.setEnabled(true);
                    }
                }, 500);
            }
        });
    }

    @OnClick(R.id.image_button_help)
    void onHelpClick() {
        if (mIsPopUpShowing) {
            mIsPopUpShowing = false;
            mPopupWindow.dismiss();
        } else {
            mHelpButton.setEnabled(false);
            mPopupWindow.showAsDropDown(mHelpButton, 0, 0);
        }
    }

    @Override
    public void updateTable(List<User> users) {
        mAdapter.updateValues(mTableDate, users);
        mIsUserUpdated = true;
    }

    @Override
    public void showProgressInView(boolean isActive) {
        mViewSwitcher.setDisplayedChild(isActive ? 1 : 0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TABLE_DATE, mTableDate.getTime());
    }

    @OnClick(R.id.image_view_back)
    void onWeekBackClick() {
        mTableDate = new Date(mTableDate.getTime() - 7 * 24 * 60 * 60 * 1000);
        updateTimeInView(getView());
        mAdapter.updateValues(mTableDate, mRoliqueAppUsers.getUsers());
    }

    @OnClick(R.id.image_view_forward)
    void onWeekNextClick() {
        mTableDate = new Date(mTableDate.getTime() + 7 * 24 * 60 * 60 * 1000);
        updateTimeInView(getView());
        mAdapter.updateValues(mTableDate, mRoliqueAppUsers.getUsers());
    }
}
