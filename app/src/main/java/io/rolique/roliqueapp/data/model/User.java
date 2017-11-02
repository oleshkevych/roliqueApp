package io.rolique.roliqueapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

import io.rolique.roliqueapp.util.DateUtil;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class User implements Parcelable {

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
    @Exclude
    private List<CheckIn> mCheckIns = new ArrayList<>();

    public User() {
    }

    public User(String id,
                String email,
                String firstName,
                String lastName,
                String imageUrl) {
        mId = id;
        mFirstName = firstName;
        mLastName = lastName;
        mImageUrl = imageUrl;
        mType = "user";
        mEmail = email;
    }

    public User(Parcel in) {
        mId = in.readString();
        mFirstName = in.readString();
        mLastName = in.readString();
        mImageUrl = in.readString();
        mType = in.readString();
        mEmail = in.readString();
        in.readTypedList(mCheckIns, CheckIn.CREATOR);
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
    public String getEmail() {
        return mEmail;
    }

    @Exclude
    public void setEmail(String email) {
        mEmail = email;
    }

    @Exclude
    public String getType() {
        return mType;
    }

    @Exclude
    public void setType(String type) {
        mType = type;
    }

    @Exclude
    public List<CheckIn> getCheckIns() {
        return mCheckIns;
    }

    @Exclude
    public void setCheckIns(List<CheckIn> checkIns) {
        mCheckIns = checkIns == null ? new ArrayList<CheckIn>() : checkIns;
    }

    @Exclude
    public void addCheckIn(CheckIn checkIn) {
        if (mCheckIns.contains(checkIn)) return;
        mCheckIns.add(checkIn);
    }

    @Exclude
    @Nullable
    public CheckIn getCheckInByDayOfYear(int dayOfYear) {
        for (CheckIn checkIn : mCheckIns)
            if (DateUtil.getDayOfYear(DateUtil.transformDate(checkIn.getTime())) == dayOfYear)
                return checkIn;
        return null;
    }


    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mFirstName);
        dest.writeString(mLastName);
        dest.writeString(mImageUrl);
        dest.writeString(mType);
        dest.writeString(mEmail);
        dest.writeTypedList(mCheckIns);
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
