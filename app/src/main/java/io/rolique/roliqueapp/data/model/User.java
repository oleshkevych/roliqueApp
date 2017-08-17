package io.rolique.roliqueapp.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class User {

    @PropertyName("id")
    public String mId;
    @PropertyName("first_name")
    public String mFirstName;
    @PropertyName("last_name")
    public String mLastName;
    @PropertyName("image_url")
    public String mImageUrl;
    @PropertyName("type")
    public String mType;
    @PropertyName("email")
    public String mEmail;

    public User() {
    }

    public User(String id,
                String email,
                String firstName,
                String lastName) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mImageUrl = "imageUrl";
        mType = "user";
        mEmail = email;
    }

    public User(String id,
                String firstName,
                String lastName,
                String imageUrl,
                String type) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mImageUrl = imageUrl;
        mType = type;
    }

    @Exclude
    public String getId() {
        return mId;
    }

    @Exclude
    public void setId(String id) {
        mId = id;
    }

    @Exclude
    public String getFirstName() {
        return mFirstName;
    }

    @Exclude
    public void setFirstName(String firstName) {
        mFirstName = firstName;
    }

    @Exclude
    public String getLastName() {
        return mLastName;
    }

    @Exclude
    public void setLastName(String lastName) {
        mLastName = lastName;
    }

    @Exclude
    public String getImageUrl() {
        return mImageUrl;
    }

    @Exclude
    public void setImageUrl(String imageUrl) {
        this.mImageUrl = imageUrl;
    }

    @Exclude
    public String getType() {
        return mType;
    }

    @Exclude
    public void setType(String type) {
        mType = type;
    }

    @Override
    public String toString() {
        return "User{" +
                "mId='" + mId + '\'' +
                ", mFirstName='" + mFirstName + '\'' +
                ", mLastName='" + mLastName + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                ", mType='" + mType + '\'' +
                ", mEmail='" + mEmail + '\'' +
                '}';
    }
}
