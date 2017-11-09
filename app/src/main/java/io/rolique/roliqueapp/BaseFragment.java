package io.rolique.roliqueapp;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Volodymyr Oleshkevych on 8/18/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public abstract class BaseFragment extends Fragment {

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inject();
    }

    protected abstract void inject();

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(BaseFragment.this, view);
    }

    @Override
    public void onDestroyView() {
        mUnbinder.unbind();
        super.onDestroyView();
    }

    protected void hideKeyboard() {
        if (getActivity() == null) return;
        View view = getActivity().getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    protected void showSnackbar(View view, String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }
}
