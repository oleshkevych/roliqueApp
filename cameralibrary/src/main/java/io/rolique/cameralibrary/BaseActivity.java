package io.rolique.cameralibrary;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public abstract class BaseActivity extends AppCompatActivity {

    ViewGroup mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        mContent = (ViewGroup) findViewById(android.R.id.content);
    }

    protected abstract void inject();

    protected void hideKeyboard() {
        View view = getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void showKeyboard() {
        View view = getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    protected <T extends View> T getViewById(@IdRes int id) {
        return (T) findViewById(id);
    }

    protected <T extends View> T getViewById(View view, @IdRes int id) {
        return (T) view.findViewById(id);
    }

    protected void showSnackbar(@StringRes int textRes, @IdRes int layoutId) {
        View layout = getViewById(layoutId);
        Snackbar.make(layout, textRes, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackbar(String text, @IdRes int layoutId) {
        View layout = getViewById(layoutId);
        Snackbar.make(layout, text, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackbar(@StringRes int textRes) {
        Snackbar.make(mContent, textRes, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackbar(String text) {
        Snackbar.make(mContent, text, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackbar(View view, String text) {
        Snackbar.make(mContent, text, Snackbar.LENGTH_LONG).show();
    }

    protected void showSnackbar(View view, @StringRes int textRes) {
        Snackbar.make(mContent, textRes, Snackbar.LENGTH_LONG).show();
    }
}
