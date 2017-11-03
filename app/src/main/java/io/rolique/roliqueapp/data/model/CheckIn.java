package io.rolique.roliqueapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Created by Volodymyr Oleshkevych on 11/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class CheckIn implements Parcelable {

    @Exclude
    public static final String CHECK_IN = "Check-in";
    @Exclude
    public static final String REMOTELY = "Remotely";
    @Exclude
    public static final String BUSINESS_TRIP = "Business trip";
    @Exclude
    public static final String DAY_OFF = "Day off";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CHECK_IN,
            REMOTELY,
            BUSINESS_TRIP,
            DAY_OFF
    })

    public @interface Type {
    }

    @Exclude
    public String mUserId;
    @PropertyName("time")
    public String mTime;
    @PropertyName("type")
    public String mType;

    public CheckIn() {
    }

    public CheckIn(String time,
                   @Type String type) {
        mTime = time;
        mType = type;
    }

    public CheckIn(Parcel in) {
        mUserId = in.readString();
        mTime = in.readString();
        mType = in.readString();
    }

    @Exclude
    public String getUserId() {
        return mUserId;
    }

    @Exclude
    public void setUserId(String userId) {
        mUserId = userId;
    }

    @Exclude
    public String getTime() {
        return mTime;
    }

    @Exclude
    public void setTime(String time) {
        mTime = time;
    }

    @Exclude
    public @Type String getType() {
        return mType;
    }

    @Exclude
    public void setType(@Type String type) {
        mType = type;
    }

    @Exclude
    public int getDayOfYear() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        String[] datesNumbers = mTime.split("_");
        calendar.set(Integer.valueOf(datesNumbers[2]), Integer.valueOf(datesNumbers[1]), Integer.valueOf(datesNumbers[0]));
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static final Parcelable.Creator<CheckIn> CREATOR = new Parcelable.Creator<CheckIn>() {
        @Override
        public CheckIn createFromParcel(Parcel in) {
            return new CheckIn(in);
        }

        @Override
        public CheckIn[] newArray(int size) {
            return new CheckIn[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserId);
        dest.writeString(mTime);
        dest.writeString(mType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckIn checkIn = (CheckIn) o;

        if (!mTime.equals(checkIn.mTime)) return false;
        return mType.equals(checkIn.mType);
    }

    @Override
    public int hashCode() {
        int result = mTime.hashCode();
        result = 31 * result + mType.hashCode();
        return result;
    }
}
