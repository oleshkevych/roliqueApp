package io.rolique.roliqueapp.screens.navigation.chats;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.util.LinksBuilder;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatsPresenter implements ChatsContract.Presenter, FirebaseValues {

    private final ChatsContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseDatabase mDatabase;
    DatabaseReference mUserChatsRef;

    @Inject
    ChatsPresenter(RoliqueApplicationPreferences preferences, ChatsContract.View view, FirebaseDatabase database) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        mUserChatsRef.removeEventListener(mUserChatsEventListener);
    }

    @Override
    public void setUpChatsListener() {
        mUserChatsRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, mPreferences.getId()));
        mUserChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChildren()) {
                    mUserChatsRef.addChildEventListener(mUserChatsEventListener);
                } else {
                    mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT))
                            .child(mPreferences.getId())
                            .child("main")
                            .setValue("true", new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                    mUserChatsRef.addChildEventListener(mUserChatsEventListener);
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
                mView.showErrorInView(databaseError.getMessage());
            }
        });

    }

    ChildEventListener mUserChatsEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, dataSnapshot.getKey()));
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    mView.showAddedChatInView(chat);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mView.showErrorInView(databaseError.getMessage());
                    Timber.e(databaseError.getMessage(), databaseError.getDetails());
                    databaseError.toException().printStackTrace();
                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, dataSnapshot.getKey()));
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    mView.showChangedChatInView(chat);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mView.showErrorInView(databaseError.getMessage());
                    Timber.e(databaseError.getMessage(), databaseError.getDetails());
                    databaseError.toException().printStackTrace();
                }
            });
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, dataSnapshot.getKey()));
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    mView.showRemovedChatInView(chat);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    mView.showErrorInView(databaseError.getMessage());
                    Timber.e(databaseError.getMessage(), databaseError.getDetails());
                    databaseError.toException().printStackTrace();
                }
            });
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Timber.e(databaseError.getMessage(), databaseError.getDetails());
            databaseError.toException().printStackTrace();
            mView.showErrorInView(databaseError.getMessage());
        }
    };

}
