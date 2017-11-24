package io.rolique.roliqueapp.widget;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.data.model.CheckIn;

/**
 * Created by Volodymyr Oleshkevych on 11/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class CheckInDialog extends Dialog {

    public interface OnCheckInAction {
        void onCheckInClick(@CheckIn.Type String type);

        void onLateMessageSelected(String message);
    }

    @BindView(R.id.drawable_text_view_title) DrawableTextView mTitleDrawableTextView;
    @BindView(R.id.drawable_text_view_message) TextView mMessageTextView;
    @BindView(R.id.container_check_ins) LinearLayout mContainerCheckInsButtons;
    @BindView(R.id.container_late) LinearLayout mContainerLateButtons;
    @BindView(R.id.text_view_check_in) TextView mCheckInTextView;
    @BindView(R.id.button_remotely) Button mRemotelyButton;
    @BindView(R.id.button_ok) Button mOkButton;
    @BindView(R.id.button_late) Button mLateButton;

    private final boolean mIsInRange;
    private final OnCheckInAction mOnCheckInAction;
    private final double mDistance;
    private final double DISTANCE_SPEED_COEF = 150 / 36;

    public CheckInDialog(@NonNull Context context,
                         boolean isInRange,
                         OnCheckInAction onCheckInAction,
                         double distance) {
        super(context);
        setCancelable(false);
        mIsInRange = isInRange;
        mOnCheckInAction = onCheckInAction;
        mDistance = distance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.content_dialog_check_in);
        ButterKnife.bind(CheckInDialog.this);
        setMessage();
        if (mIsInRange) {
            mOkButton.setVisibility(View.VISIBLE);
            mCheckInTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnCheckInAction.onCheckInClick(CheckIn.CHECK_IN);
                    dismiss();
                }
            });
        } else {
            mLateButton.setVisibility(View.VISIBLE);
            mRemotelyButton.setVisibility(View.VISIBLE);
            mCheckInTextView.setVisibility(View.GONE);
        }
    }

    private void setMessage() {
        int message = mIsInRange ? R.string.activity_navigation_check_in_in_range :
                R.string.activity_navigation_check_in_outside_range;
        mMessageTextView.setText(message);
    }

    @OnClick({R.id.button_ok, R.id.button_remotely})
    void onOkClick(View v) {
        mMessageTextView.setVisibility(View.GONE);
        mContainerCheckInsButtons.setVisibility(View.VISIBLE);
        mContainerLateButtons.setVisibility(View.GONE);
        v.setVisibility(View.GONE);
        mTitleDrawableTextView.setText(R.string.dialog_check_in_title);
        mTitleDrawableTextView.setDrawableLeft(R.drawable.ic_my_location_black_24dp);
        if (!mIsInRange)
            mLateButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.button_cancel)
    void onCancelClick() {
        dismiss();
    }

    @OnClick(R.id.text_view_remotely)
    void onRemotelyClick() {
        mOnCheckInAction.onCheckInClick(CheckIn.REMOTELY);
        dismiss();
    }

    @OnClick(R.id.text_view_business)
    void onBusinessClick() {
        mOnCheckInAction.onCheckInClick(CheckIn.BUSINESS_TRIP);
        dismiss();
    }

    @OnClick(R.id.text_view_day_off)
    void onDayOffClick() {
        mOnCheckInAction.onCheckInClick(CheckIn.DAY_OFF);
        dismiss();
    }

    @OnClick(R.id.button_late)
    void onLateClick() {
        mMessageTextView.setVisibility(View.GONE);
        mContainerCheckInsButtons.setVisibility(View.GONE);
        mContainerLateButtons.setVisibility(View.VISIBLE);
        mLateButton.setVisibility(View.GONE);
        mRemotelyButton.setVisibility(View.VISIBLE);
        mTitleDrawableTextView.setText(R.string.dialog_check_button_late);
        mTitleDrawableTextView.setDrawableLeft(R.drawable.ic_timer_black_24dp);
    }

    @OnClick(R.id.text_view_location)
    void onLocationClick() {
        long neededTime = (long) ((mDistance / DISTANCE_SPEED_COEF) * 1000);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(calendar.getTimeInMillis() + neededTime));
        showTimePickerDialog(calendar);
    }

    @OnClick(R.id.text_view_set_time)
    void onSetTimeClick() {
        showTimePickerDialog(Calendar.getInstance());
    }

    private void showTimePickerDialog(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                getContext(),
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setTitle(R.string.dialog_check_button_late);
        timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getContext().getString(R.string.dialog_check_in_confirm), timePickerDialog);
        timePickerDialog.show();
    }

    TimePickerDialog.OnTimeSetListener onTimeSetListener
            = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String hour = String.valueOf(hourOfDay);
            if (hourOfDay < 10) hour = "0" + hour;
            String minutes = String.valueOf(minute);
            if (minute < 10) minutes = "0" + minutes;
            sendMessage(String.format("%s:%s", hour, minutes));
        }
    };

    private void sendMessage(String format) {
        String message = getContext().getString(R.string.dialog_check_late_message);
        mOnCheckInAction.onLateMessageSelected(String.format("%s %s", message, format));
        dismiss();
    }
}
