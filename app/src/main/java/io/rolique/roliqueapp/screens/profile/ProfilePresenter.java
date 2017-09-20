package io.rolique.roliqueapp.screens.profile;

import android.util.Pair;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ProfilePresenter implements ProfileContract.Presenter, FirebaseValues {

    private final ProfileContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseDatabase mDatabase;
    DatabaseReference mUserDataReference;
    ChildEventListener mChildEventListener;

    @Inject
    ProfilePresenter(RoliqueApplicationPreferences preferences,
                     ProfileContract.View view,
                     FirebaseDatabase database) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
    }

    @Override
    public void start() {
        if (mUserDataReference != null)
            mUserDataReference.addChildEventListener(mChildEventListener);
    }

    @Override
    public void stop() {
        if (mUserDataReference != null)
            mUserDataReference.removeEventListener(mChildEventListener);
    }

    @Override
    public void getUserData(User user) {
        DatabaseReference dataRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH, USER_DATA, user.getId()));
        mChildEventListener = dataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                List<Pair<String, String>> pairs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    pairs.add(new Pair<>(postSnapshot.getKey(), postSnapshot.getValue(String.class)));
                mView.showValuesInView(dataSnapshot.getKey(), pairs);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                List<Pair<String, String>> pairs = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    pairs.add(new Pair<>(postSnapshot.getKey(), postSnapshot.getValue(String.class)));
                mView.showValuesInView(dataSnapshot.getKey(), pairs);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                mView.showRemoveCategoryInView(dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void updateUser(User user) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USERS,
                        user.getId()));
        dataRef.setValue(user);
    }

    @Override
    public void setNewValue(User user, String category, String key, String value) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category,
                        key));
        dataRef.setValue(value);
    }

    @Override
    public void removeValue(User user, String category, String key) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category,
                        key));
        dataRef.removeValue();
    }

    @Override
    public void removeCategory(User user, String category) {
        DatabaseReference dataRef = mDatabase.getReference(
                LinksBuilder.buildUrl(
                        AUTH,
                        USER_DATA,
                        user.getId(),
                        category));
        dataRef.removeValue();
    }

    @Override
    public void findChat(final String userId, final String profileId, final String profileImageUrl, final String profileName) {
        final DatabaseReference userChatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, mPreferences.getId()));
        userChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    final List<String> userChatIds = new ArrayList<>();
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                        userChatIds.add(postSnapshot.getKey());
                    extractUserChats(userChatIds, new ArrayList<Chat>(), userId, profileId, profileImageUrl, profileName);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                mView.showErrorInView(databaseError.getMessage());
            }
        });
    }

    private void extractUserChats(final List<String> userChatIds, final List<Chat> chats, final String userId, final String profileId, final String profileImageUrl, final String profileName) {
        if (userChatIds.size() == 0) {
            checkIfChatExists(chats, userId, profileId, profileImageUrl, profileName);
        } else {
            final DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, userChatIds.get(0)));
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat != null) {
                        chats.add(chat);
                    }
                    userChatIds.remove(0);
                    extractUserChats(userChatIds, chats, userId, profileId, profileImageUrl, profileName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                    mView.showErrorInView(databaseError.getMessage());
                }
            });
        }
    }

    private void checkIfChatExists(List<Chat> chats, String userId, String profileId, String profileImageUrl, String profileName) {
        for (Chat chat : chats)
            if (chat.getMemberIds().size() == 2
                    && chat.getMemberIds().contains(userId)
                    && chat.getMemberIds().contains(profileId)) {
                mView.showChatInView(chat);
                return;
            }
        mView.showChatInView(createNewChat(userId, profileId, profileImageUrl, profileName));
    }

    private Chat createNewChat(final String userId, final String profileId, String profileImageUrl, String profileName) {
        Chat chat = new Chat.Builder()
                .setImageUrl(profileImageUrl)
                .setMemberIds(new ArrayList<String>(Arrays.asList(profileId, userId)))
                .setOwnerId(userId)
                .setTitle(profileName)
                .create();
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS)).push();
        String chatId = chatRef.getKey();
        chat.setId(chatId);

        DatabaseReference chatRef1 = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, chat.getId()));
        chatRef1.setValue(chat);
        Message chatMessage = new Message(chatId, mPreferences.getId(), "Welcome!", DateUtil.getStringTime(), "user");
        DatabaseReference messageInChatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chatId)).push();
        String id = messageInChatRef.getKey();
        chatMessage.setId(id);

        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chatId, chatMessage.getId()));
        messageRef.setValue(chatMessage);
        for (String memberId : chat.getMemberIds()) {
            DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chatId));
            memberRef.setValue(chatMessage);
        }
        return chat;
    }
}
