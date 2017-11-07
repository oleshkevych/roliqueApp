package io.rolique.roliqueapp.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Volodymyr Oleshkevych on 8/28/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class Chat implements Parcelable {

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };
    @PropertyName("id")
    public String mId;
    @PropertyName("image_url")
    public String mImageUrl;
    @PropertyName("is_single")
    public boolean mIsSingle;
    @PropertyName("members")
    public List<String> mMemberIds = new ArrayList<>();
    @PropertyName("owner")
    public String mOwnerId;
    @PropertyName("title")
    public String mTitle;
    @Exclude
    private Message mLastMessage;
    @Exclude
    private boolean mHasNewMessages;

    public Chat() {
    }

    public Chat(String id,
                String imageUrl,
                List<String> memberIds,
                String ownerId,
                String title,
                boolean isSingle) {
        mId = id;
        mImageUrl = imageUrl;
        mMemberIds = memberIds;
        mOwnerId = ownerId;
        mTitle = title;
        mIsSingle = isSingle;
    }

    public Chat(Parcel in) {
        mId = in.readString();
        mImageUrl = in.readString();
        in.readStringList(mMemberIds);
        mOwnerId = in.readString();
        mTitle = in.readString();
        mIsSingle = in.readInt() == 1;
        mLastMessage = in.readParcelable(Message.class.getClassLoader());
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
    public String getImageUrl() {
        return mImageUrl;
    }

    @Exclude
    public void setImageUrl(String imageUrl) {
        mImageUrl = imageUrl;
    }

    @Exclude
    public List<String> getMemberIds() {
        return mMemberIds;
    }

    @Exclude
    public void setMemberIds(List<String> memberIds) {
        mMemberIds = memberIds;
    }

    @Exclude
    public String getOwnerId() {
        return mOwnerId;
    }

    @Exclude
    public void setOwnerId(String ownerId) {
        mOwnerId = ownerId;
    }

    @Exclude
    public String getTitle() {
        return mTitle;
    }

    @Exclude
    public void setTitle(String title) {
        mTitle = title;
    }

    @Exclude
    public boolean isSingle() {
        return mIsSingle;
    }

    @Exclude
    public void setSingle(boolean single) {
        mIsSingle = single;
    }

    @Exclude
    public boolean isHasNewMessages() {
        return mHasNewMessages;
    }

    @Exclude
    public void setHasNewMessages(boolean hasNewMessages) {
        mHasNewMessages = hasNewMessages;
    }

    @Exclude
    public Message getLastMessage() {
        return mLastMessage;
    }

    @Exclude
    public void setLastMessage(Message lastMessage) {
        mLastMessage = lastMessage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mImageUrl);
        dest.writeStringList(mMemberIds);
        dest.writeString(mOwnerId);
        dest.writeString(mTitle);
        dest.writeInt(mIsSingle ? 1 :0);
        dest.writeParcelable(mLastMessage, flags);
    }

    @Override
    public String toString() {
        return "Chat{" +
                "mId='" + mId + '\'' +
                ", mImageUrl='" + mImageUrl + '\'' +
                ", mIsSingle=" + mIsSingle +
                ", mMemberIds=" + mMemberIds +
                ", mOwnerId='" + mOwnerId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mLastMessage=" + mLastMessage +
                '}';
    }

    public static class Builder {

        public String mId;
        public String mImageUrl;
        public List<String> mMemberIds = new ArrayList<>();
        public String mOwnerId;
        public String mTitle;
        public boolean mIsSingle;

        public Builder setId(String id) {
            mId = id;
            return this;
        }

        public Builder setImageUrl(String imageUrl) {
            mImageUrl = imageUrl;
            return this;
        }

        public Builder setMemberIds(List<String> memberIds) {
            mMemberIds = memberIds;
            return this;
        }

        public Builder setOwnerId(String ownerId) {
            mOwnerId = ownerId;
            return this;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setSingle(boolean isSingle) {
            mIsSingle = isSingle;
            return this;
        }

        public Chat create() {
            return new Chat(mId,
                    mImageUrl,
                    mMemberIds,
                    mOwnerId,
                    mTitle,
                    mIsSingle);
        }
    }
}

