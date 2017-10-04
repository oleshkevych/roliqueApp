package io.rolique.roliqueapp.widget;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
public class AddPickerDialog extends BottomSheetDialogFragment {

    private static final String ARGUMENT_CATEGORY = "ARGUMENT_CATEGORY";

    public static AddPickerDialog newInstance(String category) {
        AddPickerDialog dialog = new AddPickerDialog();
        Bundle args = new Bundle();
        args.putString(ARGUMENT_CATEGORY, category);
        dialog.setArguments(args);
        return dialog;
    }

    public interface OnPickListener {
        void onSaveClick(AddPickerDialog dialog, String category, String key, String value);
        void onCancelLinkClick(AddPickerDialog dialog);
    }

    @BindView(R.id.text_view_category) TextView mCategoryTextView;
    @BindView(R.id.edit_text_key) EditText mKeyEditText;
    @BindView(R.id.edit_text_value) EditText mValueEditText;
    @BindView(R.id.text_view_save) TextView mSaveTextView;

    private Unbinder mUnbinder;
    private OnPickListener mPickListener;
    private String mCategory;

    @Nullable
    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_picker, container, false);
    }

    @Override public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(AddPickerDialog.this, view);
        mCategory = getArguments().getString(ARGUMENT_CATEGORY);
        mCategoryTextView.setText(mCategory);
        mKeyEditText.addTextChangedListener(mTextWatcher);
        mValueEditText.addTextChangedListener(mTextWatcher);
    }

    TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            toggleVisibility(mKeyEditText.getText().length() > 0 && mValueEditText.getText().length() > 0);
        }
    };

    private void toggleVisibility(boolean isSaveVisible) {
        mSaveTextView.setAlpha(isSaveVisible ? 1.0f : 0.2f);
        mSaveTextView.setOnClickListener(isSaveVisible ? mOnClickListener : null);
    }

    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mPickListener != null) mPickListener.onSaveClick(AddPickerDialog.this,
                    mCategory,
                    mKeyEditText.getText().toString(),
                    mValueEditText.getText().toString());
        }
    };

    @OnClick(R.id.text_view_cancel)
    protected void onOpenLinkClick() {
        if (mPickListener != null) mPickListener.onCancelLinkClick(AddPickerDialog.this);
    }

    public void setOnPickListener(OnPickListener pickListener) {
        mPickListener = pickListener;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
