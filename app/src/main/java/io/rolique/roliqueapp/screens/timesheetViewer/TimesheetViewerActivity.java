package io.rolique.roliqueapp.screens.timesheetViewer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.screens.timesheetViewer.adapters.SampleTableAdapter;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.widget.fixedHeaderTable.TableFixHeaders;

/**
 * Created by Volodymyr Oleshkevych on 10/31/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class TimesheetViewerActivity extends BaseActivity implements TimesheetContract.View {

    public static Intent startIntent(Context context) {
        return new Intent(context, TimesheetViewerActivity.class);
    }

    private static final String TABLE_DATE = "DATE";

    @Inject TimesheetPresenter mPresenter;
    @Inject RoliqueAppUsers mRoliqueAppUsers;

    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    SampleTableAdapter mAdapter;
    Date mTableDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timesheet_viewer);

        mTableDate = new Date();
        if (savedInstanceState != null)
            mTableDate = new Date(savedInstanceState.getLong(TABLE_DATE));
        setUpToolbar();
        updateTimeInView();
        setUpTableView();
        mPresenter.fetchTimesheetsByDate(new Date());
    }

    @Override
    protected void inject() {
        DaggerTimesheetComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .timesheetPresenterModule(new TimesheetPresenterModule(TimesheetViewerActivity.this))
                .build()
                .inject(TimesheetViewerActivity.this);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.activity_timesheet_viewer_title));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void updateTimeInView() {
        ((TextView) findViewById(R.id.text_view_table_date)).setText(getTableTime());
    }

    private String getTableTime() {
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

    private void setUpTableView() {
        TableFixHeaders tableFixHeaders = findViewById(R.id.table);
        tableFixHeaders.setRowSelectable(false);
        mAdapter = new SampleTableAdapter(TimesheetViewerActivity.this, mRoliqueAppUsers.getUsers());
        tableFixHeaders.setAdapter(mAdapter);
    }

    @Override
    public void updateTable(List<User> users) {
        mAdapter.updateValues(mTableDate, users);
    }

    @Override
    public void showProgressInView(boolean isActive) {
        mProgressBar.setVisibility(isActive ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(TABLE_DATE, mTableDate.getTime());
    }

    @OnClick(R.id.image_view_back)
    void onWeekBackClick() {
        mTableDate = new Date(mTableDate.getTime() - 7 * 24 * 60 * 60 * 1000);
        updateTimeInView();
        mAdapter.updateValues(mTableDate, mRoliqueAppUsers.getUsers());
    }

    @OnClick(R.id.image_view_forward)
    void onWeekNextClick() {
        mTableDate = new Date(mTableDate.getTime() + 7 * 24 * 60 * 60 * 1000);
        updateTimeInView();
        mAdapter.updateValues(mTableDate, mRoliqueAppUsers.getUsers());
    }
}
