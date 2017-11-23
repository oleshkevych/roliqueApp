package io.rolique.roliqueapp;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Singleton;

import io.rolique.roliqueapp.data.model.User;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Singleton
public class RoliqueApplicationPreferences {

    private static final String KEY_ID = "ID";
    private static final String KEY_FIRST_NAME = "FIRST_NAME";
    private static final String KEY_LAST_NAME = "LAST_NAME";
    private static final String KEY_IMAGE_URL = "IMAGE_URL";
    private static final String KEY_TYPE = "TYPE";
    private static final String KEY_LOGGED_IN = "LOGGED_IN";

    public interface UserChangesListener {
        void onInfoChanged();
    }

    private UserChangesListener mListener;
    private final SharedPreferences mSharedPreferences;
    private final Context mContext;

    RoliqueApplicationPreferences(Context context) {
        mContext = context;
        mSharedPreferences = context
                .getApplicationContext()
                .getSharedPreferences(context.getString(R.string.extra_shared_preferences), Context.MODE_PRIVATE);
    }

    public void setListener(UserChangesListener listener) {
        mListener = listener;
    }

    public void logIn(User user) {
        setId(user.getId());
        setFirstName(user.getFirstName());
        setLastName(user.getLastName());
        setImageUrl(user.getImageUrl());
        setType(user.getType());
        setLoggedIn(true);
        if (mListener != null)
            mListener.onInfoChanged();
    }

    public String getId() {
        return mSharedPreferences.getString(KEY_ID, "");
    }

    private void setId(String email) {
        mSharedPreferences.edit()
                .putString(KEY_ID, email)
                .apply();
    }

    private void setType(String token) {
        mSharedPreferences.edit()
                .putString(KEY_TYPE, token)
                .apply();
    }

    public String getLoginId() {
        return mSharedPreferences.getString(KEY_TYPE, "");
    }

    public String getFirstName() {
        return mSharedPreferences.getString(KEY_FIRST_NAME, "");
    }

    private void setFirstName(String firstName) {
        mSharedPreferences.edit()
                .putString(KEY_FIRST_NAME, firstName)
                .apply();
    }

    public String getLastName() {
        return mSharedPreferences.getString(KEY_LAST_NAME, "");
    }

    private void setLastName(String lastName) {
        mSharedPreferences.edit()
                .putString(KEY_LAST_NAME, lastName)
                .apply();
    }

    public String getImageUrl() {
        return mSharedPreferences.getString(KEY_IMAGE_URL, "");
    }

    private void setImageUrl(String imageUrl) {
        mSharedPreferences.edit()
                .putString(KEY_IMAGE_URL, imageUrl)
                .apply();
    }

    public boolean isLoggedIn() {
        return mSharedPreferences.getBoolean(KEY_LOGGED_IN, false);
    }

    private void setLoggedIn(boolean loggedIn) {
        mSharedPreferences.edit()
                .putBoolean(KEY_LOGGED_IN, loggedIn)
                .apply();
    }

    public void logOut() {
        mSharedPreferences.edit().clear().apply();
    }

    public String getNotificationTime() {
        return mSharedPreferences.getString(mContext.getString(R.string.extra_shared_preferences_notification_time), "10 45");
    }

    public void setNotificationTime(String notificationTime) {
        mSharedPreferences.edit()
                .putString(mContext.getString(R.string.extra_shared_preferences_notification_time), notificationTime)
                .apply();
    }

    public boolean isNotificationAllowed() {
        return mSharedPreferences.getBoolean(mContext.getString(R.string.extra_shared_preferences_notification_allowed), true);
    }


    public void setIsNotificationAllowed(boolean isNotificationAllowed) {
        mSharedPreferences.edit()
                .putBoolean(mContext.getString(R.string.extra_shared_preferences_notification_allowed), isNotificationAllowed)
                .apply();
    }
}
