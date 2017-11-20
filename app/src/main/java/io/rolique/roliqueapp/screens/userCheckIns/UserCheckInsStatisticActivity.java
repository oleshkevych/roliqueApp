package io.rolique.roliqueapp.screens.userCheckIns;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.RoliqueApplication;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BaseActivity;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 11/20/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class UserCheckInsStatisticActivity extends BaseActivity implements UserCheckInsStatisticContract.View {

    private static final int LAST_WEEK = 0;
    private static final int ALL_TIME = 1;

    private static String EXTRA_USER = "USER";

    public static Intent startIntent(Context context, User user) {
        Intent intent = new Intent(context, UserCheckInsStatisticActivity.class);
        intent.putExtra(EXTRA_USER, user);
        return intent;
    }

    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.text_view_header) TextView mHeaderTextView;
    @BindView(R.id.text_view_switcher) TextView mSwitcherTextView;
    @BindView(R.id.text_view_missed_per_week) TextView mMissedPerWeekTextView;
    @BindView(R.id.text_view_missed_per_month) TextView mMissedPerMonthTextView;
    @BindView(R.id.chart1) PieChart mChart;

    @Inject UserCheckInsStatisticPresenter mPresenter;

    User mUser;
    int mSelectedPeriod;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_check_ins_statistic);
        mUser = getIntent().getParcelableExtra(EXTRA_USER);
        setUpToolbar();
        setHeaderTexts();

        mPresenter.getTimesheetByTime(mUser);
    }

    @Override
    protected void inject() {
        DaggerUserCheckInsStatisticComponent.builder()
                .roliqueApplicationComponent(((RoliqueApplication) getApplication()).getRepositoryComponent())
                .userCheckInsStatisticPresenterModule(new UserCheckInsStatisticPresenterModule(UserCheckInsStatisticActivity.this))
                .build()
                .inject(UserCheckInsStatisticActivity.this);
    }

    private void setUpToolbar() {
        mToolbar.setTitle(R.string.activity_user_check_in_statistic_title);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void setHeaderTexts() {
        String text = getString(mSelectedPeriod == LAST_WEEK ? R.string.activity_user_check_in_statistic_header_weekly :
                R.string.activity_user_check_in_statistic_header_all_time);
        mHeaderTextView.setText(String.format("%s: %s", text, UiUtil.getUserNameForView(mUser)));
        mSwitcherTextView.setText(mSelectedPeriod == LAST_WEEK ? R.string.activity_user_check_in_statistic_switcher_all_time :
                R.string.activity_user_check_in_statistic_switcher_weekly);
    }

    @OnClick(R.id.text_view_switcher)
    void onHeaderClick() {
        if (mSelectedPeriod == LAST_WEEK)
            mSelectedPeriod = ALL_TIME;
        else mSelectedPeriod = LAST_WEEK;
        setHeaderTexts();
        updateChat();
    }

    private void updateChat() {
        if (mUser.getCheckIns().isEmpty()) return;
        List<CheckIn> checkIns = getCheckIns();


        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setCenterText(generateCenterSpannableText(checkIns.isEmpty()));

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(Color.WHITE);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(true);

        setData(checkIns);

        mChart.animateY(1400, Easing.EasingOption.EaseInOutQuad);

        Legend l = mChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        mChart.setEntryLabelColor(Color.DKGRAY);
        mChart.setEntryLabelTextSize(12f);
    }

    private void setData(List<CheckIn> checkIns) {

        ArrayList<PieEntry> entries = new ArrayList<>();

        int checkInOkCount = 0;
        int remotelyCount = 0;
        int lateCount = 0;
        int dayOffCount = 0;
        int businessTripCount = 0;

        for (CheckIn checkIn : checkIns) {
            switch (checkIn.getType()) {
                case CheckIn.CHECK_IN:
                    Date messageDate = DateUtil.transformDate(checkIn.getTime());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(messageDate);
                    if ((calendar.get(Calendar.HOUR_OF_DAY) > 10) ||
                            (calendar.get(Calendar.HOUR_OF_DAY) == 10 && calendar.get(Calendar.MINUTE) > 47)) {
                        lateCount++;
                    } else {
                        checkInOkCount++;
                    }
                    break;
                case CheckIn.BUSINESS_TRIP:
                    businessTripCount++;
                    break;
                case CheckIn.DAY_OFF:
                    dayOffCount++;
                    break;
                case CheckIn.REMOTELY:
                    remotelyCount++;
            }
        }

        if (checkInOkCount > 0)
            entries.add(new PieEntry((float) (checkInOkCount),
                    CheckIn.CHECK_IN,
                    getResources().getDrawable(R.drawable.item_check_in_ok)));
        if (lateCount > 0)
            entries.add(new PieEntry((float) (lateCount),
                    getString(R.string.activity_user_check_in_statistic_late),
                    getResources().getDrawable(R.drawable.item_check_in_late)));
        if (businessTripCount > 0)
            entries.add(new PieEntry((float) (businessTripCount),
                    CheckIn.BUSINESS_TRIP,
                    getResources().getDrawable(R.drawable.item_check_in_business_trip)));
        if (dayOffCount > 0)
            entries.add(new PieEntry((float) (dayOffCount),
                    CheckIn.DAY_OFF,
                    getResources().getDrawable(R.drawable.item_check_in_day_off)));
        if (remotelyCount > 0)
            entries.add(new PieEntry((float) (remotelyCount),
                    CheckIn.REMOTELY,
                    getResources().getDrawable(R.drawable.item_check_in_remotely)));

        PieDataSet dataSet = new PieDataSet(entries, getString(R.string.activity_user_check_in_statistic_results));

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.DKGRAY);
        mChart.setData(data);
        mChart.highlightValues(null);
        mChart.invalidate();
    }

    private SpannableString generateCenterSpannableText(boolean isEmpty) {

        String header = getString(R.string.activity_user_check_in_statistic_chart_title);
        if (isEmpty)
            header = getString(R.string.activity_user_check_in_statistic_chart_title_empty);
        SpannableString s = new SpannableString(header + UiUtil.getUserNameForView(mUser));
        s.setSpan(new RelativeSizeSpan(isEmpty ? 1.3f : 1.7f), 0, header.length(), 0);
        s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - UiUtil.getUserNameForView(mUser).length(), s.length(), 0);
        s.setSpan(new RelativeSizeSpan(1.3f), header.length(), s.length(), 0);
        s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - UiUtil.getUserNameForView(mUser).length(), s.length(), 0);
        return s;
    }

    @NonNull
    private List<CheckIn> getCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();
        if (mSelectedPeriod == LAST_WEEK) {
            int dayOfWeek = DateUtil.getDayOfWeek(new Date());
            if (dayOfWeek < 1) dayOfWeek = 7;
            int dayOfYear = DateUtil.getDayOfYear(new Date());
            do {
                CheckIn checkIn = mUser.getCheckInByDayOfYear(dayOfYear);
                if (checkIn != null)
                    checkIns.add(checkIn);
                dayOfYear--;
                dayOfWeek--;
            } while (dayOfWeek > 0);
        } else {
            checkIns.addAll(mUser.getCheckIns());
        }
        return checkIns;
    }

    @Override
    public void showCheckInInView(User user) {
        mUser = user;
        updateChat();
        setHeaderTexts();
        setUpMissedValues();
    }

    private void setUpMissedValues() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int dayOfWeek = DateUtil.getDayOfWeek(new Date());
                int missedDaysOfWeek = 0;
                int allDaysOfWeek = 0;
                int dayOfYear = DateUtil.getDayOfYear(new Date());
                if (dayOfWeek < Calendar.MONDAY) dayOfWeek = Calendar.FRIDAY;
                do {
                    CheckIn checkIn = mUser.getCheckInByDayOfYear(dayOfYear);
                    boolean isWeekday = ((dayOfWeek >= Calendar.MONDAY) && (dayOfWeek <= Calendar.FRIDAY));
                    if (isWeekday) {
                        if (checkIn == null)
                            missedDaysOfWeek++;
                        allDaysOfWeek++;
                    }
                    dayOfYear--;
                    dayOfWeek--;
                } while (dayOfWeek > 0);

                dayOfWeek = DateUtil.getDayOfWeek(new Date());
                int dayOfMonth = DateUtil.getDayOfMonth(new Date());
                int missedDaysOfMonth = 0;
                int allDaysOfMonth = 0;
                dayOfYear = DateUtil.getDayOfYear(new Date());
                do {
                    if (dayOfWeek < 0) dayOfWeek = Calendar.FRIDAY;
                    CheckIn checkIn = mUser.getCheckInByDayOfYear(dayOfYear);
                    boolean isWeekday = ((dayOfWeek >= Calendar.MONDAY) && (dayOfWeek <= Calendar.FRIDAY));
                    if (isWeekday) {
                        if (checkIn == null)
                            missedDaysOfMonth++;
                        allDaysOfMonth++;
                    }
                    dayOfYear--;
                    dayOfWeek--;
                    dayOfMonth--;
                } while (dayOfMonth > 0);
                final String monthDays = String.format("%s/%s", missedDaysOfMonth, allDaysOfMonth);
                final String weekDays = String.format("%s/%s", missedDaysOfWeek, allDaysOfWeek);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setMissedCheckInsValues(monthDays, weekDays);
                    }
                });
            }
        }).run();
    }

    protected void setMissedCheckInsValues(String monthDays, String weekDays) {
        mMissedPerMonthTextView.setText(String.format("%s %s", getString(R.string.activity_user_check_in_statistic_missed_per_month), monthDays));
        mMissedPerMonthTextView.setVisibility(View.VISIBLE);
        mMissedPerWeekTextView.setText(String.format("%s %s", getString(R.string.activity_user_check_in_statistic_missed_per_week), weekDays));
        mMissedPerWeekTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void showProgressInView(boolean isActive) {
        mProgressBar.setVisibility(isActive ? View.VISIBLE : View.GONE);
    }
}
