package io.rolique.roliqueapp.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import io.rolique.roliqueapp.R;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class ProfileCategoryCard extends FrameLayout {

    public interface OnValueChangeListener {
        void onValueChanged(String category, String key, String value);
    }

    public interface OnActionClickListener {
        void onCall(String number);

        void onLinkOpen(String url);

        void onTextSelected(String text, EditText editText, String category);

        void onDeleteClick(String category, String key);

        void onRemoveCategory(String category);
    }

    public interface OnKeyboardChangeListener {
        void isKeyboardShown(boolean isShown);
    }

    OnValueChangeListener mOnValueChangeListener;
    OnActionClickListener mOnActionClickListener;
    OnKeyboardChangeListener mOnKeyboardListener;
    String mCategory;
    String[] mCategories;
    List<Pair<String, String>> mPairs;
    boolean mIsEditable;

    public ProfileCategoryCard(Context context) {
        super(context);
        init(context, null);
    }

    public ProfileCategoryCard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ProfileCategoryCard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(final Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.content_category_data, this);
        initCardProperties(context);
        if (attrs == null) return;
        final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ProfileCategoryCard);
        mCategories = context.getResources().getStringArray(R.array.user_data_categories);
        mCategory = mCategories[typedArray.getInteger(R.styleable.ProfileCategoryCard_pcc_category_number, 1)];
        TextView categoryTextView = findViewById(R.id.text_view_category);
        categoryTextView.setText(mCategory);
        typedArray.recycle();
        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!mIsEditable) return false;
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage(String.format("%s %s", "Remove all info about your", mCategory));
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        if (mOnActionClickListener != null)
                            mOnActionClickListener.onRemoveCategory(mCategory);
                    }
                });
                builder.setNegativeButton(R.string.bottom_dialog_add_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
                return true;
            }
        });
    }

    private void initCardProperties(Context context) {
        setAddStatesFromChildren(true);
        setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        ViewGroup.LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        ViewGroup.MarginLayoutParams marginLayoutParams = new MarginLayoutParams(layoutParams);
        marginLayoutParams.setMargins(10, 40, 10, 40);
        setLayoutParams(marginLayoutParams);
        setVisibility(GONE);
    }

    public void setOnValueChangeListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    public void setOnActionClickListener(OnActionClickListener onActionClickListener) {
        mOnActionClickListener = onActionClickListener;
    }

    public void setOnKeyboardListener(OnKeyboardChangeListener onKeyboardListener) {
        mOnKeyboardListener = onKeyboardListener;
    }

    public void setValues(List<Pair<String, String>> pairs) {
        mPairs = pairs;
        setValueInView(pairs);
    }

    private void setValueInView(List<Pair<String, String>> pairs) {
        setVisibility(VISIBLE);
        LinearLayout containerLayout = findViewById(R.id.layout_data_container);
        containerLayout.removeAllViews();
        for (final Pair<String, String> pair : pairs) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_data, null);
            containerLayout.addView(view);
            final TextView textView = view.findViewById(R.id.text_view_data_key);
            textView.setText(pair.first);
            ImageButton deleteButton = view.findViewById(R.id.button_delete);
            deleteButton.setVisibility(mIsEditable ? VISIBLE : GONE);
            deleteButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnActionClickListener != null)
                        mOnActionClickListener.onDeleteClick(mCategory, pair.first);
                }
            });
            final KeyboardEditText editText = view.findViewById(R.id.edit_text_data_value);
            editText.setText(pair.second);
            editText.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));
            if (mIsEditable) {
                editText.setFocusable(true);
                editText.setInputType(getInputType()|InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                editText.setTextColor(ContextCompat.getColor(editText.getContext(), R.color.black));
                editText.setOnFocusChangeListener(new OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View view, boolean b) {
                        if (mOnValueChangeListener != null && mOnKeyboardListener != null)
                            if (b) mOnKeyboardListener.isKeyboardShown(true);
                            else
                                mOnValueChangeListener.onValueChanged(mCategory, textView.getText().toString(), editText.getText().toString());
                    }
                });
                editText.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editText.setCursorVisible(true);
                        mOnKeyboardListener.isKeyboardShown(true);
                    }
                });
                editText.setOnKeyboardChangeListener(new KeyboardEditText.OnKeyboardChangeListener() {
                    @Override
                    public void onKeyboardStateChanged(boolean isVisible) {
                        if (mOnKeyboardListener != null)
                            mOnKeyboardListener.isKeyboardShown(isVisible);
                    }
                });
                editText.setOnLongClickListener(null);
            } else {
                editText.setTextColor(ContextCompat.getColor(editText.getContext(), R.color.indigo_accent_700));
                editText.setFocusable(false);
                if (mCategory.equals(mCategories[2]) || mCategory.equals(mCategories[3]) || mCategory.equals(mCategories[5]))
                    editText.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnActionClickListener.onLinkOpen(editText.getText().toString());
                        }
                    });
                if (mCategory.equals(mCategories[0]))
                    editText.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mOnActionClickListener.onCall(editText.getText().toString());
                        }
                    });
                editText.setOnLongClickListener(new OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        editText.selectAll();
                        mOnActionClickListener.onTextSelected(editText.getText().toString(), editText, mCategory);
                        return true;
                    }
                });
                editText.setOnKeyboardChangeListener(null);
            }
        }
    }

    private int getInputType() {
        if (mCategory.equals(mCategories[0])) return InputType.TYPE_CLASS_PHONE;
        if (mCategory.equals(mCategories[1])) return InputType.TYPE_TEXT_FLAG_CAP_SENTENCES;
        if (mCategory.equals(mCategories[2])) return InputType.TYPE_CLASS_TEXT;
        if (mCategory.equals(mCategories[3])) return InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        if (mCategory.equals(mCategories[4])) return InputType.TYPE_CLASS_TEXT;
        if (mCategory.equals(mCategories[5])) return InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT;
        return InputType.TYPE_CLASS_TEXT;
    }

    public void setIsEditable(boolean isEditable) {
        mIsEditable = isEditable;
        if (mPairs != null) setValueInView(mPairs);
    }

    public void cleanView() {
        LinearLayout containerLayout = findViewById(R.id.layout_data_container);
        containerLayout.removeAllViews();
        mPairs = null;
        setVisibility(GONE);
    }
}
