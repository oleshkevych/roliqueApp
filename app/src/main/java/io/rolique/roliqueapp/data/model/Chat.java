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
public class Chat {

    @PropertyName("id")
    public String mId;
    @PropertyName("image_url")
    public String mImageUrl;
    @PropertyName("members")
    public List<String> mMemberIds;
    @PropertyName("owner")
    public String mOwnerId;
    @PropertyName("title")
    public String mTitle;

    public Chat() {
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
}

