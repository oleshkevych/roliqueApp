package io.rolique.roliqueapp.screens.editChat;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatEditorPresenter implements ChatEditorContract.Presenter, FirebaseValues {

    private final ChatEditorContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseDatabase mDatabase;

    @Inject
    ChatEditorPresenter(RoliqueApplicationPreferences preferences,
                        ChatEditorContract.View view,
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
    }

    @Override
    public void saveNewChat(final Chat chat, Bitmap image) {
        mView.setProgressIndicator(true);
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS)).push();
        String id = chatRef.getKey();
        chat.setId(id);

        uploadImage(chat, null, image);
    }


    @Override
    public void editChat(Chat newChat, Chat oldChat, Bitmap image) {
        mView.setProgressIndicator(true);
        uploadImage(newChat, oldChat, image);
    }

    private void uploadImage(final Chat newChat, final Chat oldChat, Bitmap image) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s.jpg", new Date().getTime()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.PNG, 80, baos);
        byte[] data = baos.toByteArray();

        storageRef.putBytes(data)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        exception.printStackTrace();
                        mView.showErrorInView(exception.getMessage());
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                        newChat.setImageUrl(downloadUrl);
                        if (oldChat == null) setChat(newChat);
                        else updateChat(newChat, oldChat);
                    }
                });
    }

    private void setChat(Chat chat) {
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, chat.getId()));
        chatRef.setValue(chat);
        setUpMembers(chat.getMemberIds(), chat.getId());
    }

    private void setUpMembers(List<String> memberIds, String chatId) {
        Message chatMessage = new Message(chatId, mPreferences.getId(), "Welcome!", DateUtil.getStringTime(), "user");
        DatabaseReference messageInChatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chatId)).push();
        String id = messageInChatRef.getKey();
        chatMessage.setId(id);

        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chatId, chatMessage.getId()));
        messageRef.setValue(chatMessage);
        for (String memberId : memberIds)
            connectChatToMember(chatId, memberId);
        mView.setProgressIndicator(false);
        mView.showSavedInView();
    }

    private void connectChatToMember(String chatId, String memberId) {
        Message chatMessage = new Message(chatId, mPreferences.getId(), "Welcome!", DateUtil.getStringTime(), "user");
        DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chatId));
        memberRef.setValue(chatMessage);
    }

    private void updateChat(Chat newChat, Chat oldChat) {
        List<String> oldMemberIds = oldChat.getMemberIds();
        for (String newMemberId : newChat.getMemberIds())
            if (oldMemberIds.contains(newMemberId)) {
                oldMemberIds.remove(newMemberId);
            } else {
                connectChatToMember(newChat.getId(), newMemberId);
            }
        for (String removedMemberId : oldMemberIds)
            disconnectChat(oldChat.getId(), removedMemberId);
        updateChatValues(newChat);
    }

    private void disconnectChat(String chatId, String memberId) {
        DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, chatId));
        memberRef.removeValue();
    }

    private void updateChatValues(Chat newChat) {
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, newChat.getId()));
        setValue(chatRef, TITLE, newChat.getTitle());
        setValue(chatRef, IMAGE, newChat.getImageUrl());
        setValue(chatRef, MEMBERS, newChat.getMemberIds());
        mView.showSavedInView();
    }

    private void setValue(DatabaseReference chatRef, String child, Object value) {
        chatRef.child(child).setValue(value);
    }

    @Override
    public void deleteChat(Chat chat) {
        for (String memberId : chat.getMemberIds())
            disconnectChat(chat.getId(), memberId);
        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId()));
        messageRef.removeValue();
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, chat.getId()));
        chatRef.removeValue();
        mView.showSavedInView();
    }
}
