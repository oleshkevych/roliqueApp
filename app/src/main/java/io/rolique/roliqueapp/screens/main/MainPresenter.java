package io.rolique.roliqueapp.screens.main;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.rolique.roliqueapp.screens.BasePresenter;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class MainPresenter implements BasePresenter {

    private final MainContract.View mView;

    private CompositeDisposable mCompositeDisposable;

    @Inject
    public MainPresenter(MainContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
