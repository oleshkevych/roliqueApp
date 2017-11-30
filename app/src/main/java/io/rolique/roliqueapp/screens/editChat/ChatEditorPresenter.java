package io.rolique.roliqueapp.screens.editChat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueAppUsers;
import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.LinksBuilder;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class ChatEditorPresenter implements ChatEditorContract.Presenter, FirebaseValues {

    private final ChatEditorContract.View mView;

    private RoliqueApplicationPreferences mPreferences;

    FirebaseDatabase mDatabase;
    RoliqueAppUsers mRoliqueAppUsers;
    List<User> mUserWithTokens;

    @Inject
    ChatEditorPresenter(RoliqueApplicationPreferences preferences,
                        ChatEditorContract.View view,
                        FirebaseDatabase database,
                        RoliqueAppUsers roliqueAppUsers) {
        mView = view;
        mPreferences = preferences;
        mDatabase = database;
        mRoliqueAppUsers = roliqueAppUsers;
    }

    @Override
    public void start() {
        if (mUserWithTokens != null) return;
        mView.setProgressIndicator(true);
        if (mRoliqueAppUsers.getUsers().isEmpty()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    start();
                }
            }, 500);
            return;
        }
        fetchUserTokens();
    }

    private void fetchUserTokens() {
        DatabaseReference tokensRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_TOKEN));
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> users = new ArrayList<>(mRoliqueAppUsers.getUsers());
                mUserWithTokens = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (User user : users)
                        if (user.getId().equals(snapshot.getKey())) {
                            String token = snapshot.child(TOKEN).getValue().toString();
                            assert token != null;
                            Timber.e(user.getId() + " " + token);
                            user.setFirebaseToken(token);
                            mUserWithTokens.add(user);
                            break;
                        }
                    mView.showUserInView(users);
                    mView.setProgressIndicator(false);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void stop() {
    }

    @Override
    public void saveNewChat(final Chat chat) {
        mView.setProgressIndicator(true);
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS)).push();
        String id = chatRef.getKey();
        chat.setId(id);
        mView.subscribeMembersInView(chat);
        uploadImage(chat, null);
    }


    @Override
    public void editChat(Chat newChat, Chat oldChat) {
        mView.setProgressIndicator(true);
        uploadImage(newChat, oldChat);
    }

    private void uploadImage(final Chat newChat, final Chat oldChat) {
        if (newChat.getImageUrl().isEmpty() || newChat.getImageUrl().startsWith("https")) {
            if (oldChat == null) setChat(newChat);
            else updateChat(newChat, oldChat);
        } else {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s.jpg", new Date().getTime()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(newChat.getImageUrl(), bmOptions);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
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
    }

    private void setChat(Chat chat) {
        DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, chat.getId()));
        chatRef.setValue(chat);
        setUpMembers(chat.getMemberIds(), chat);
    }

    private void setUpMembers(List<String> memberIds, Chat chat) {
        Message chatMessage = new Message(chat.getId(), mPreferences.getId(), "Welcome!", DateUtil.getStringTime(), "user");
        DatabaseReference messageInChatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId())).push();
        String id = messageInChatRef.getKey();
        chatMessage.setId(id);

        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, chat.getId(), chatMessage.getId()));
        messageRef.setValue(chatMessage);
        for (String memberId : memberIds)
            connectChatToMember(chat.getId(), memberId);
        mView.showSavedInView(chat);
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
        mView.showSavedInView(newChat);
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
        mView.showSavedInView(chat);
    }

    @Override
    public void fetchMutedUsers(final Chat chat) {
        DatabaseReference tokensRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_MUTES));
        tokensRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<User> unMutedMemberUsers = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    for (User user : mUserWithTokens)
                        if (user.getId().equals(snapshot.getKey())) {
                            if (snapshot.child(chat.getId()).getValue() != null) {
                                boolean isMuted = snapshot.child(chat.getId()).getValue(Boolean.class);
                                Timber.e(user.getId() + " " + isMuted);
                                if (!isMuted) unMutedMemberUsers.add(user);
                            } else {
                                unMutedMemberUsers.add(user);
                            }
                            break;
                        }
                }
                mView.setUnMutedUsers(chat, unMutedMemberUsers);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void subscribeMembers(Context context, Chat chat, List<User> unMutedMemberUsers) {
        sendRequest(context, chat, unMutedMemberUsers, true, false);
    }

    @Override
    public void deleteChatSubscribtion(Context context, Chat chat) {
        List<User> memberUsers = new ArrayList<>();
        for (User user : mUserWithTokens)
            if (chat.getMemberIds().contains(user.getId()))
                memberUsers.add(user);
        sendRequest(context, chat, memberUsers, false, true);
    }

    void sendRequest(Context context, Chat chat, List<User> users, boolean isSubscribe, boolean isDeleteChat) {
        SubscribeManager subscribeManager = new SubscribeManager(context, chat);
        if (subscribeManager.sendSubscribeRequest(createTokenList(users), isSubscribe))
            for (User user : users) {
                DatabaseReference muteRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_MUTES, user.getId(), chat.getId()));
                if (isDeleteChat) muteRef.removeValue();
                else muteRef.setValue(!isSubscribe);
            }
        mView.showFinishAsyncInView();
    }

    private List<String> createTokenList(List<User> users) {
        List<String> tokens = new ArrayList<>();
        for (User user : users)
            tokens.add(user.getFirebaseToken());
        return tokens;
    }
}
