package io.rolique.cameralibrary.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.StringDef;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by Volodymyr Oleshkevych on 9/21/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class MediaContent implements Parcelable {

    public static final String CATEGORY_VIDEO = "video";
    public static final String CATEGORY_IMAGE = "photo";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            CATEGORY_IMAGE,
            CATEGORY_VIDEO
    })

    public @interface Category {
    }

    private String mImage;
    private String mVideo;
    private int mHeight;
    private int mWidth;

    private @Category String mMediaType;

    public MediaContent() {
    }

    public MediaContent(String file, int height, int width, String mediaType) {
        if (mediaType.equals(CATEGORY_IMAGE)) mImage = file;
        else mVideo = file;
        mHeight = height;
        mWidth = width;
        mMediaType = mediaType;
    }

    public MediaContent(String image, String video, int height, int width, String mediaType) {
        mImage = image;
        mVideo = video;
        mHeight = height;
        mWidth = width;
        mMediaType = mediaType;
    }

    public MediaContent(Parcel in) {
        mImage = in.readString();
        mVideo = in.readString();
        mHeight = in.readInt();
        mWidth = in.readInt();
        mMediaType = in.readString();
    }

    public String getImage() {
        return mImage;
    }

    public void setImage(String image) {
        mImage = image;
    }

    public String getVideo() {
        return mVideo;
    }

    public void setVideo(String video) {
        mVideo = video;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public boolean isVideo() {
        return mMediaType.equals(CATEGORY_VIDEO);
    }

    public boolean isImage() {
        return mMediaType.equals(CATEGORY_IMAGE);
    }

    public
    @Category
    String getMediaType() {
        return mMediaType;
    }

    public void setMediaType(String mediaType) {
        mMediaType = mediaType;
    }

    @Override
    public String toString() {
        return "Media{" +
                "mImage='" + mImage + '\'' +
                ", mVideo='" + mVideo + '\'' +
                ", mHeight='" + mHeight + '\'' +
                ", mWidth='" + mWidth + '\'' +
                ", mMediaType='" + mMediaType + '\'' +
                '}';
    }

    public static final class Builder {

        private String mImage;
        private String mVideo;
        private int mHeight;
        private int mWidth;
        private @Category String mMediaType;

        public Builder setImage(String image) {
            mImage = image;
            return this;
        }

        public Builder setVideo(String video) {
            mVideo = video;
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

        public MediaContent create() {
            return new MediaContent(mImage,
                    mVideo,
                    mHeight,
                    mWidth,
                    mMediaType);
        }
    }

    public static final Creator<MediaContent> CREATOR = new Creator<MediaContent>() {
        @Override
        public MediaContent createFromParcel(Parcel in) {
            return new MediaContent(in);
        }

        @Override
        public MediaContent[] newArray(int size) {
            return new MediaContent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImage);
        dest.writeString(mVideo);
        dest.writeInt(mHeight);
        dest.writeInt(mWidth);
        dest.writeString(mMediaType);
    }
}
