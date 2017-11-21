package io.rolique.roliqueapp.widget;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 4/6/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ReasonDialogFragment extends DialogFragment {

    public static String TAG = ReasonDialogFragment.class.getSimpleName();

    public static ReasonDialogFragment getFragment() {
        return new ReasonDialogFragment();
    }

    public interface OnClickListener {
        void onButtonClick(String title);
    }

    OnClickListener mOnClickListener;

    @NonNull
    @Override
    @SuppressLint("InflateParams")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_reason, null);
        setCancelable(false);
        final TextInputEditText mReasonEditText = view.findViewById(R.id.edit_text_title);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String reasonText = mReasonEditText.getText().toString();
                        if (mOnClickListener != null)
                            mOnClickListener.onButtonClick(reasonText);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOnClickListener.onButtonClick("");
                    }
                });
        return builder.create();
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }
}
