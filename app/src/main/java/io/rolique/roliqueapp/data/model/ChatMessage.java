package io.rolique.roliqueapp.data.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.PropertyName;

import java.util.List;

/**
 * Created by Volodymyr Oleshkevych on 8/28/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@IgnoreExtraProperties
public class ChatMessage {

    @PropertyName("last_message")
    public String mLastMessage;
    @PropertyName("sender")
    public String mSenderId;
    @PropertyName("time_stamp")
    public String mTimeStamp;

    public ChatMessage() {
    }

    @Exclude
    public String getLastMessage() {
        return mLastMessage;
    }

    @Exclude
    public void setLastMessage(String lastMessage) {
        mLastMessage = lastMessage;
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
    public String getTimeStamp() {
        return mTimeStamp;
    }

    @Exclude
    public void setTimeStamp(String timeStamp) {
        mTimeStamp = timeStamp;
    }

    public void setEmpty() {
        mLastMessage = "";
        mTimeStamp = "";
        mSenderId = "";
    }
}

