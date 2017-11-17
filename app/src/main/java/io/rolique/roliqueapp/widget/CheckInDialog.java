package io.rolique.roliqueapp.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    }

    @BindView(R.id.drawable_text_view_message) TextView mMessageTextView;
    @BindView(R.id.container_check_ins) LinearLayout mContainerCheckInsButtons;
    @BindView(R.id.text_view_check_in) TextView mCheckInTextView;

    private final boolean mIsInRange;
    private final OnCheckInAction mOnCheckInAction;

    public CheckInDialog(@NonNull Context context, boolean isInRange, OnCheckInAction onCheckInAction) {
        super(context);
        setCancelable(false);
        mIsInRange = isInRange;
        mOnCheckInAction = onCheckInAction;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.content_dialog_check_in);
        ButterKnife.bind(CheckInDialog.this);
        setMessage();
        if (mIsInRange)
            mCheckInTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnCheckInAction.onCheckInClick(CheckIn.CHECK_IN);
                    dismiss();
                }
            });
        else
            mCheckInTextView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black_alpha_50));
    }

    private void setMessage() {
        int message = mIsInRange ? R.string.activity_navigation_check_in_in_range :
                R.string.activity_navigation_check_in_outside_range;
        mMessageTextView.setText(message);
    }

    @OnClick(R.id.button_ok)
    void onOkClick(View v) {
        mMessageTextView.setVisibility(View.GONE);
        mContainerCheckInsButtons.setVisibility(View.VISIBLE);
        v.setVisibility(View.GONE);
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
}
