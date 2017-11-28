package io.rolique.roliqueapp.screens.chat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.LinksBuilder;
import io.rolique.roliqueapp.util.ui.UiUtil;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatPresenter implements ChatContract.Presenter, FirebaseValues {

    private final ChatContract.View mView;

    RoliqueApplicationPreferences mPreferences;
    FirebaseDatabase mDatabase;
    Query mChatQuery;

    boolean mIsProgressActive;
    boolean mIsDeleting;
    Message mLastMessage;

    SenderNotificationRequestManager mSenderNotificationRequestManager;

    @Inject
    ChatPresenter(RoliqueApplicationPreferences preferences,
                  ChatContract.View view,
                  FirebaseDatabase database) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {
        mChatQuery.removeEventListener(mChildEventListener);
    }

    @Override
    public void getTopMessages(String firstMessageId, Chat chat) {
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES));
        Query chatQuery = chatRef.child(chat.getId()).orderByKey().endAt(firstMessageId).limitToFirst(20);
        chatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    messages.add(postSnapshot.getValue(Message.class));
                messages.remove(messages.size() - 1);
                mView.showTopMessagesView(messages, messages.size() == 19);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mView.showErrorInView(databaseError.toException().getMessage());
            }
        });

    }

    @Override
    public void fetchLastMessages(Context context, Chat chat) {
        mSenderNotificationRequestManager = new SenderNotificationRequestManager(context, chat, getUserName());
        mIsProgressActive = true;
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES));
        mChatQuery = chatRef.child(chat.getId()).limitToLast(20);
        mChatQuery.addChildEventListener(mChildEventListener);
    }

    private String getUserName() {
        return String.format("%s %s", mPreferences.getFirstName(), mPreferences.getLastName());
    }


    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (mIsProgressActive) {
                mIsProgressActive = false;
            }
            Message message = dataSnapshot.getValue(Message.class);
            assert message != null;
            if (mIsDeleting && DateUtil.isFirstEarlier(mLastMessage.getTimeStamp(), message.getTimeStamp())) {
                mIsDeleting = false;
                return;
            }
            mLastMessage = message;
            mView.showNewMessageView(message);
            setLastMessageStatus(mPreferences.getId(), message.getChatId(), false);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);
            mView.updateMessageView(message);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mView.removedMessageView(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (mIsProgressActive) {
                mIsProgressActive = false;
            }
            mView.showErrorInView(databaseError.toException().getMessage());
        }
    };

    @Override
    public void leaveChat(Chat chat, String memberId) {
        chat.getMemberIds().remove(memberId);
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, chat.getId()));
        chatRef.setValue(chat);
        DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chat.getId()));
        memberRef.removeValue();
        mView.showLeaveInView();
    }

    @Override
    public void setMessage(final Message message, final Chat chat) {
        if (message.getId() == null) {
            DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId())).push();
            String id = chatRef.getKey();
            message.setId(id);
            for (String memberId : chat.getMemberIds()) {
                DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chat.getId()));
                memberRef.setValue(message);
                setLastMessageStatus(memberId, chat.getId(), true);
            }
            setLastMessageStatus(mPreferences.getId(), chat.getId(), false);
            notifyAllMembers(message);
        } else {
            DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, chat.getMemberIds().get(0), chat.getId()));
            memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Message message1 = dataSnapshot.getValue(Message.class);
                    if (message1 == null) return;
                    if (message1.getId().equals(message.getId()))
                        for (String memberId : chat.getMemberIds()) {
                            DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chat.getId()));
                            memberRef.setValue(message);
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId(), message.getId()));
        messageRef.setValue(message);

        if (message.getMedias().size() > 0)
            uploadRecycle(message, messageRef);
    }

    private void uploadRecycle(final Message message, final DatabaseReference messageRef) {
        messageRef.setValue(message);
        int index = -1;
        for (int i = 0; i < message.getMedias().size(); i++)
            if (!message.getMedias().get(i).getImageUrl().startsWith("http")) {
                index = i;
                break;
            } else if (message.getMedias().get(i).isVideo() && !message.getMedias().get(i).getVideoUrl().startsWith("http")) {
                index = i;
                break;
            }

        if (index == -1) return;
        final boolean isVideoDownloading = message.getMedias().get(index).isVideo() &&
                message.getMedias().get(index).getImageUrl().startsWith("http");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s%s", new Date().getTime(), (isVideoDownloading ? ".mp4" : ".jpg")));
        UploadTask uploadTask;
        if (isVideoDownloading) {
            Uri uri = Uri.fromFile(new File(message.getMedias().get(index).getVideoUrl()));
            uploadTask = storageRef.putFile(uri);
        } else {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(message.getMedias().get(index).getImageUrl(), bmOptions);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
            byte[] data = baos.toByteArray();

            uploadTask = storageRef.putBytes(data);
        }
        final int finalIndex = index;
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                if (isVideoDownloading)
                    message.getMedias().get(finalIndex).setVideoUrl(downloadUrl);
                else
                    message.getMedias().get(finalIndex).setImageUrl(downloadUrl);
                uploadRecycle(message, messageRef);
            }
        });
    }

    private void notifyAllMembers(Message message) {
        mSenderNotificationRequestManager.sendMessage(message);
    }

    @Override
    public void removeMessage(final Message message, final Chat chat, final boolean isInLast20th) {
        mIsDeleting = true;
        setLastMessageStatus(mPreferences.getId(), chat.getId(), false);
        final DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, chat.getMemberIds().get(0), chat.getId()));
        memberRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Message message1 = dataSnapshot.getValue(Message.class);
                assert message1 != null;
                if (message1.getId().equals(message.getId())) {
                    Query messageQuery = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId())).limitToLast(2);
                    messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                Message message1 = dataSnapshot1.getValue(Message.class);
                                if (!message.getId().equals(message1.getId()))
                                    for (String memberId : chat.getMemberIds()) {
                                        DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chat.getId()));
                                        memberRef.setValue(message1);
                                    }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId(), message.getId()));
                messageRef.removeValue();
                if (!isInLast20th) {
                    mView.removedMessageView(message.getId());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setLastMessageStatus(String userId, String chatId, boolean isNotRead) {
        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_NEW_MESSAGES, userId, chatId));
        messageRef.setValue(isNotRead);
    }
}
