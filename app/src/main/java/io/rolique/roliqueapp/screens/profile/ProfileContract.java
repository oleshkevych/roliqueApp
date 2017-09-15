package io.rolique.roliqueapp.screens.profile;

import android.util.Pair;

import java.util.List;

import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;

/**
 * Created by Volodymyr Oleshkevych on 9/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
interface ProfileContract {

    interface View extends BaseView<Presenter> {
        void showValuesInView(String category, List<Pair<String, String>> pairs);
        void showRemoveCategoryInView(String category);
        void showErrorInView(String message);
    }

    interface Presenter extends BasePresenter {
        void getUserData(User user);
        void updateUser(User user);
        void setNewValue(User user, String category, String key, String value);
        void removeValue(User user, String category, String key);
        void removeCategory(User user, String category);
    }
}
