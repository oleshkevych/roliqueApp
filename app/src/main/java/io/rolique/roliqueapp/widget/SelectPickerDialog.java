package io.rolique.roliqueapp.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 9/15/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class SelectPickerDialog extends BottomSheetDialogFragment {

    private static final String ARGUMENT_FIELD_TYPE = "ARGUMENT_FIELD_TYPE";
    private static final String EMPTY = "EMPTY";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";
    public static final String PHONE_NUMBER_AND_SELECTION = "PHONE_NUMBER_AND_SELECTION";
    public static final String LINK = "LINK";
    public static final String LINK_AND_SELECTION = "LINK_AND_SELECTION";
    public static final String EMAIL_AND_SELECTION = "EMAIL_AND_SELECTION";

    public static SelectPickerDialog newInstance() {
        SelectPickerDialog dialog = new SelectPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_FIELD_TYPE, EMPTY);
        dialog.setArguments(args);
        return dialog;
    }

    public static SelectPickerDialog newInstance(String fieldToShow) {
        SelectPickerDialog dialog = new SelectPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_FIELD_TYPE, fieldToShow);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnPickListener {
        void onCallClick(SelectPickerDialog dialog);
        void onMailClick(SelectPickerDialog dialog);
        void onOpenLinkClick(SelectPickerDialog dialog);
        void onCopyClick(SelectPickerDialog dialog);
        void onShareClick(SelectPickerDialog dialog);
        void onCancelSelection();
    }

    @BindView(R.id.text_view_open_link) TextView mOpenLinkTextView;
    @BindView(R.id.text_view_email) TextView mEmailTextView;
    @BindView(R.id.text_view_call) TextView mCallTextView;
    @BindView(R.id.text_view_copy) TextView mCopyTextView;
    @BindView(R.id.text_view_share) TextView mShareTextView;

    private Unbinder mUnbinder;
    private OnPickListener mPickListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_select_picker, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(SelectPickerDialog.this, view);
        String fieldToShowType = getArguments().getString(ARGUMENT_FIELD_TYPE);
        showExtraField(fieldToShowType);
    }

    private void showExtraField(String fieldToShowType) {
        switch (fieldToShowType) {
            case PHONE_NUMBER:
                mCallTextView.setVisibility(View.VISIBLE);
                break;
            case LINK:
                mOpenLinkTextView.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                mCopyTextView.setVisibility(View.VISIBLE);
                mShareTextView.setVisibility(View.VISIBLE);
                break;
            case LINK_AND_SELECTION:
                mOpenLinkTextView.setVisibility(View.VISIBLE);
                mCopyTextView.setVisibility(View.VISIBLE);
                mShareTextView.setVisibility(View.VISIBLE);
                break;
            case PHONE_NUMBER_AND_SELECTION:
                mCallTextView.setVisibility(View.VISIBLE);
                mCopyTextView.setVisibility(View.VISIBLE);
                mShareTextView.setVisibility(View.VISIBLE);
                break;
            case EMAIL_AND_SELECTION:
                mEmailTextView.setVisibility(View.VISIBLE);
                mCopyTextView.setVisibility(View.VISIBLE);
                mShareTextView.setVisibility(View.VISIBLE);
                break;
        }
    }

    @OnClick(R.id.text_view_call)
    protected void onCallClick() {
        if (mPickListener != null) mPickListener.onCallClick(SelectPickerDialog.this);
    }

    @OnClick(R.id.text_view_email)
    protected void onEmailClick() {
        if (mPickListener != null) mPickListener.onMailClick(SelectPickerDialog.this);
    }

    @OnClick(R.id.text_view_open_link)
    protected void onOpenLinkClick() {
        if (mPickListener != null) mPickListener.onOpenLinkClick(SelectPickerDialog.this);
    }

    @OnClick(R.id.text_view_copy)
    protected void onCopyClick() {
        if (mPickListener != null) mPickListener.onCopyClick(SelectPickerDialog.this);
    }

    @OnClick(R.id.text_view_share)
    protected void onShareClick() {
        if (mPickListener != null) mPickListener.onShareClick(SelectPickerDialog.this);
    }

    public void setOnPickListener(OnPickListener pickListener) {
        mPickListener = pickListener;
    }

    @Override
    public void onDestroyView() {
        mPickListener.onCancelSelection();
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
