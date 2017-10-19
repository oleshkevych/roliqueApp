package io.rolique.roliqueapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Volodymyr Oleshkevych on 9/20/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class Media implements Parcelable {
    @Exclude
    public static final String CATEGORY_VIDEO = "video";
    @Exclude
    public static final String CATEGORY_IMAGE = "photo";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CATEGORY_IMAGE,
            CATEGORY_VIDEO
    })

    public @interface Category {
    }

    @PropertyName("image_url")
    public String mImageUrl;
    @PropertyName("video_url")
    public String mVideoUrl;
    @PropertyName("height")
    public int mHeight;
    @PropertyName("width")
    public int mWidth;

    @PropertyName("type")
    public @Category String mMediaType;

    public Media() {
    }

    public Media(String imageUrl, int height, int width, String mediaType) {
        mImageUrl = imageUrl;
        mHeight = height;
        mWidth = width;
        mMediaType = mediaType;
    }

    public Media(String imageUrl, String videoUrl, int height, int width, String mediaType) {
        mImageUrl = imageUrl;
        mVideoUrl = videoUrl;
        mHeight = height;
        mWidth = width;
        mMediaType = mediaType;
    }

    public Media(Parcel in) {
        mImageUrl = in.readString();
        mVideoUrl = in.readString();
        mHeight = in.readInt();
        mWidth = in.readInt();
        mMediaType = in.readString();
    }

    @Exclude
    public String getImageUrl() {
        return mImageUrl;
    }

    @Exclude
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Exclude
    public String getVideoUrl() {
        return mVideoUrl;
    }

    @Exclude
    public void setVideoUrl(String videoUrl) {
        mVideoUrl = videoUrl;
    }

    @Exclude
    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    @Exclude
    public int getWidth() {
        return mWidth;
    }

    @Exclude
    public void setWidth(int width) {
        mWidth = width;
    }

    @Exclude
    public
    @Category
    String getMediaType() {
        return mMediaType;
    }

    @Exclude
    public void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    @Exclude
    public boolean isVideoType() {
        return mMediaType.equals(CATEGORY_VIDEO);
    }

    @Override
    public String toString() {
        return "Media{" +
                "mImageUrl='" + mImageUrl + '\'' +
                ", mVideoUrl='" + mVideoUrl + '\'' +
                ", mHeight='" + mHeight + '\'' +
                ", mWidth='" + mWidth + '\'' +
                ", mMediaType='" + mMediaType + '\'' +
                '}';
    }

    public static final Creator<Media> CREATOR = new Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel in) {
            return new Media(in);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageUrl);
        dest.writeString(mVideoUrl);
        dest.writeInt(mHeight);
        dest.writeInt(mWidth);
        dest.writeString(mMediaType);
    }

    public static final class Builder {

        private String mImageUrl;
        private String mVideoUrl;
        private int mHeight;
        private int mWidth;
        private @Category String mMediaType;

        public Builder setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
            return this;
        }

        public Builder setVideoUrl(String videoUrl) {
            mVideoUrl = videoUrl;
            return this;
        }

        public Builder setHeight(int height) {
            mHeight = height;
            return this;
        }

        public Builder setWidth(int width) {
            mWidth = width;
            return this;
        }

        public Builder setMediaType(String mediaType) {
            mMediaType = mediaType;
            return this;
        }

        public Media create() {
            return new Media(mImageUrl,
                    mVideoUrl,
                    mHeight,
                    mWidth,
                    mMediaType);
        }
    }
}
