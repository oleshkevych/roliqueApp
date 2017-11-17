package io.rolique.roliqueapp.screens.navigation.chats;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;
import io.rolique.roliqueapp.util.ui.UiUtil;
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
    RoliqueAppUsers mRoliqueAppUsers;

    @Inject
    ChatsPresenter(RoliqueApplicationPreferences preferences,
                   ChatsContract.View view,
                   FirebaseDatabase database,
                   RoliqueAppUsers roliqueAppUsers) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
        mRoliqueAppUsers = roliqueAppUsers;
    }

    @Override
    public void start() {
        if (mUserChatsRef != null)
            mUserChatsRef.addChildEventListener(mUserChatsEventListener);
    }

    @Override
    public void stop() {
        if (mUserChatsRef != null)
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
//                } else {
//                    mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT))
//                            .child(mPreferences.getId())
//                            .child("main")
//                            .setValue("true", new DatabaseReference.CompletionListener() {
//                                @Override
//                                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                                    mUserChatsRef.addChildEventListener(mUserChatsEventListener);
//                                }
//                            });
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
            final DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, dataSnapshot.getKey()));
            chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat == null) return;
                    if (chat.isSingle()) setSingleProperties(chat);
                    DatabaseReference userChatsRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, mPreferences.getId(), chat.getId()));
                    userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Message message = null;
                            if (dataSnapshot.getValue() instanceof String) {
                                message = Message.getStartMessage(chat.getId(), mPreferences.getId());
//                                DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, message.getChatId())).push();
//                                String id = chatRef.getKey();
//                                message.setId(id);

//                                DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId(), message.getId()));
//                                messageRef.setValue(message);
                            } else {
                                message = dataSnapshot.getValue(Message.class);
                            }
                            chat.setLastMessage(message);
                            checkIfSeen(chat, true);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
                    final Chat chat = dataSnapshot.getValue(Chat.class);
                    DatabaseReference userChatsRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, mPreferences.getId(), chat.getId()));
                    userChatsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Message message = dataSnapshot.getValue(Message.class);
                            chat.setLastMessage(message);
                            checkIfSeen(chat, false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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
                    mView.showRemovedChatInView(dataSnapshot.getKey());
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
            if (!databaseError.getDetails().contains("permission"))
                mView.showErrorInView(databaseError.getMessage());
        }
    };

    private void setSingleProperties(Chat chat) {
        for (String id : chat.getMemberIds())
            if (!id.equals(mPreferences.getId()))
                for (User user : mRoliqueAppUsers.getUsers())
                    if (user.getId().equals(id)) {
                        chat.setImageUrl(user.getImageUrl());
                        chat.setTitle(UiUtil.getUserNameForView(user));
                        return;
                    }

    }

    private void checkIfSeen(final Chat chat, final boolean isNewMessage) {
        DatabaseReference userNewMessageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_NEW_MESSAGES, mPreferences.getId(), chat.getId()));
        userNewMessageRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean isNotSeen = dataSnapshot.getValue(Boolean.class);
                chat.setHasNewMessages(isNotSeen == null ? true : isNotSeen);
                if (isNewMessage) mView.showAddedChatInView(chat);
                else mView.showChangedChatInView(chat);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
