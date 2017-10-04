package io.rolique.roliqueapp.screens.chat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.util.LinksBuilder;

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
        mView.setProgressIndicator(true);
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES));
        Query chatQuery = chatRef.child(chat.getId()).orderByKey().endAt(firstMessageId).limitToFirst(20);
        chatQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Message> messages = new ArrayList<>();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren())
                    messages.add(postSnapshot.getValue(Message.class));
                messages.remove(messages.size() - 1);
                mView.showTopMessagesView(messages);
                mView.setProgressIndicator(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                mView.showErrorInView(databaseError.toException().getMessage());
            }
        });

    }

    @Override
    public void fetchLastMessages(Chat chat) {
        mView.setProgressIndicator(true);
        mIsProgressActive = true;
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES));
        mChatQuery = chatRef.child(chat.getId()).limitToLast(20);
        mChatQuery.addChildEventListener(mChildEventListener);
    }

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            if (mIsProgressActive) {
                mIsProgressActive = false;
                mView.setProgressIndicator(false);
            }
            Message message = dataSnapshot.getValue(Message.class);
            mView.showNewMessageView(message);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            if (mIsProgressActive) {
                mIsProgressActive = false;
                mView.setProgressIndicator(false);
            }
            mView.showErrorInView(databaseError.toException().getMessage());
        }
    };

    @Override
    public void addMessage(Message message, Chat chat) {
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, message.getChatId())).push();
        String id = chatRef.getKey();
        message.setId(id);

        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId(), message.getId()));
        messageRef.setValue(message);

        for (String memberId : chat.getMemberIds()) {
            DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chat.getId()));
            memberRef.setValue(message);
        }
    }

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
    public void addMediaMessage(Message message, Chat chat) {
        //TODO: add progress
        uploadRecycle(0, message, chat);
    }

    private void uploadRecycle(final int countUploading, final Message message, final Chat chat) {
        if (message.getMedias().size() == countUploading) {
            addMessage(message, chat);
            return;
        }
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s.jpg", new Date().getTime()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(message.getMedias().get(countUploading).getImageUrl(), bmOptions);
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = storageRef.putBytes(data);
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
                message.getMedias().get(countUploading).setImageUrl(downloadUrl);
                uploadRecycle(countUploading + 1, message, chat);
            }
        });
    }
}
