package io.rolique.roliqueapp.widget.recyclerPicker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 9/19/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RecyclerPickerDialog extends BottomSheetDialogFragment {

    private static final String ARGUMENT_TITLE = "ARGUMENT_TITLE";
    private static final String ARGUMENT_VALUES = "ARGUMENT_VALUES";

    public static RecyclerPickerDialog newInstance(String[] array, @StringRes int title) {
        RecyclerPickerDialog dialog = new RecyclerPickerDialog();
        Bundle args = new Bundle();
        args.putInt(ARGUMENT_TITLE, title);
        args.putStringArray(ARGUMENT_VALUES, array);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnPickListener {
        void onValueClick(RecyclerPickerDialog dialog, String value);
    }

    @BindView(R.id.text_view_title) TextView mTitleTextView;
    @BindView(R.id.recycler_view_values) RecyclerView mValuesRecyclerView;

    private Unbinder mUnbinder;
    private OnPickListener mPickListener;

    @Nullable
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_recycler_picker, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(RecyclerPickerDialog.this, view);
        String[] valuesToShow = getArguments().getStringArray(ARGUMENT_VALUES);
        mTitleTextView.setText(getArguments().getInt(ARGUMENT_TITLE));
        showValues(valuesToShow);
    }

    private void showValues(String[] values) {
        mValuesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ValuesAdapter mUsersAdapter = new ValuesAdapter(getContext(), values);
        mValuesRecyclerView.setAdapter(mUsersAdapter);
        mUsersAdapter.setOnItemClickListener(new ValuesAdapter.OnItemClickListener() {
            @Override
            public void onValueClick(String value) {
                mPickListener.onValueClick(RecyclerPickerDialog.this, value);
            }
        });
    }

    public void setOnPickListener(OnPickListener pickListener) {
        mPickListener = pickListener;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
