package io.rolique.roliqueapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

import io.rolique.roliqueapp.util.DateUtil;

/**
 * Created by Volodymyr Oleshkevych on 9/4/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class Message implements Parcelable{

    @Exclude
    public static Message getStartMessage(String chatId, String userId) {
        return new Message(chatId, userId, "Welcome!", DateUtil.getStringTime(), "user");
    }

    public static Message createMediaMessage(String chatId, String userId, List<Media> medias) {
        return new Message(chatId, userId, "Welcome!", DateUtil.getStringTime(), "user", medias);
    }

    @PropertyName("id")
    public String mId;
    @PropertyName("chat_id")
    public String mChatId;
    @PropertyName("sender")
    public String mSenderId;
    @PropertyName("text")
    public String mText;
    @PropertyName("time_stamp")
    public String mTimeStamp;
    @PropertyName("type")
    public String mType;
    @PropertyName("images")
    public List<Media> mMedias = new ArrayList<>();

    public Message() {
    }

    public Message(String chatId,
                   String senderId,
                   String text,
                   String timeStamp,
                   String type) {
        mChatId = chatId;
        mSenderId = senderId;
        mText = text;
        mTimeStamp = timeStamp;
        mType = type;
    }

    public Message(String chatId, String senderId, String text, String timeStamp, String type, List<Media> medias) {
        mChatId = chatId;
        mSenderId = senderId;
        mText = text;
        mTimeStamp = timeStamp;
        mType = type;
        mMedias = medias;
    }

    public Message(Parcel in) {
        mId = in.readString();
        mChatId = in.readString();
        mSenderId = in.readString();
        mText = in.readString();
        mTimeStamp = in.readString();
        mType = in.readString();
        in.readTypedList(mMedias, Media.CREATOR);
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
    public String getChatId() {
        return mChatId;
    }

    @Exclude
    public void setChatId(String chatId) {
        mChatId = chatId;
    }

    @Exclude
    public String getSenderId() {
        return mSenderId;
    }

    @Exclude
    public void setSenderId(String senderId) {
        mSenderId = senderId;
    }

    @Exclude
    public String getText() {
        return mText;
    }

    @Exclude
    public void setText(String text) {
        mText = text;
    }

    @Exclude
    public String getTimeStamp() {
        return mTimeStamp;
    }

    @Exclude
    public void setTimeStamp(String timeStamp) {
        mTimeStamp = timeStamp;
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
    public List<Media> getMedias() {
        return mMedias;
    }

    @Exclude
    public boolean isMedia() {
        return mMedias.size() > 0;
    }

    @Exclude
    public void setMedias(List<Media> medias) {
        mMedias = medias;
    }

    @Override
    public String toString() {
        return "Message{" +
                "mId='" + mId + '\'' +
                ", mChatId='" + mChatId + '\'' +
                ", mSenderId='" + mSenderId + '\'' +
                ", mText='" + mText + '\'' +
                ", mTimeStamp='" + mTimeStamp + '\'' +
                ", mType='" + mType + '\'' +
                '}';
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mChatId);
        dest.writeString(mSenderId);
        dest.writeString(mText);
        dest.writeString(mTimeStamp);
        dest.writeString(mType);
        dest.writeTypedList(mMedias);
    }

    public static class Builder {

        private String mChatId;
        private String mSenderId;
        private String mText;
        private String mTimeStamp;
        private String mType;
        private List<Media> mMedias = new ArrayList<>();

        public Builder setChatId(String chatId) {
            mChatId = chatId;
            return this;
        }

        public Builder setSenderId(String senderId) {
            mSenderId = senderId;
            return this;
        }

        public Builder setText(String text) {
            mText = text;
            return this;
        }

        public Builder setTimeStamp(String timeStamp) {
            mTimeStamp = timeStamp;
            return this;
        }

        public Builder setType(String type) {
            mType = type;
            return this;
        }

        public Builder setMedias(List<Media> medias) {
            mMedias = medias;
            return this;
        }

        public Message create() {
            return new Message(mChatId,
                    mSenderId,
                    mText,
                    mTimeStamp,
                    mType,
                    mMedias);
        }
    }
}
