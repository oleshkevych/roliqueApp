package io.rolique.roliqueapp.screens.navigation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.CheckIn;
import io.rolique.roliqueapp.data.model.Media;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.util.DateUtil;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class NavigationPresenter implements NavigationContract.Presenter, FirebaseValues {

    private final NavigationActivity mView;

    final RoliqueApplicationPreferences mPreferences;
    final FirebaseAuth mAuth;
    final FirebaseDatabase mDatabase;
    final ConnectivityManager mConnectivityManager;

    @Inject
    NavigationPresenter(RoliqueApplicationPreferences preferences,
                        NavigationActivity view,
                        FirebaseAuth auth,
                        FirebaseDatabase database,
                        ConnectivityManager connectivityManager) {
        mView = view;
        mPreferences = preferences;
        mAuth = auth;
        mDatabase = database;
        mConnectivityManager = connectivityManager;
    }

    @Override
    public void start() {
        if (mPreferences.isLoggedIn()) {
            mPreferences.setListener(mListener);
            mView.setImage(mPreferences.getImageUrl(), getName());
            mView.setUserName(getName());
        }
    }

    protected String getName() {
        return String.format("%s %s", mPreferences.getFirstName(), mPreferences.getLastName());
    }

    @Override
    public void stop() {
        mPreferences.setListener(null);
    }

    @Override
    public void isLogin() {
        mView.showLoginInView(mPreferences.isLoggedIn());
    }

    private RoliqueApplicationPreferences.UserChangesListener mListener = new RoliqueApplicationPreferences.UserChangesListener() {
        @Override
        public void onInfoChanged() {
            mView.setImage(mPreferences.getImageUrl(), getName());
            mView.setUserName(getName());
        }
    };

    @Override
    public void logout() {
        mAuth.signOut();
        mPreferences.logOut();
        mPreferences.setListener(null);
        isLogin();
    }

    @Override
    public void updateUserPicture(Media media) {
        if (lackInternetConnection()) {
            mView.showConnectionErrorInView();
            return;
        }
        mView.setImageProgress(true);
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s.jpg", new Date().getTime()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(media.getImageUrl(), bmOptions);
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
                @SuppressWarnings("VisibleForTests") String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                updateUserInfo(downloadUrl);
            }
        });
    }

    private void updateUserInfo(String imagePath) {
        DatabaseReference imageRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH, USERS, mPreferences.getId(), "image_url"));
        imageRef.setValue(imagePath);
        mView.setImageProgress(false);
    }


    @Override
    public void checkIfUserCheckedIn() {
        if (lackInternetConnection()) {
            mView.showConnectionErrorInView();
            return;
        }
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        DatabaseReference reference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN, mDateFormat.format(new Date())));
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean isCheckedIn = false;
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    if (dataSnapshot1.getKey().equals(mPreferences.getId())) {
                        isCheckedIn = true;
                        break;
                    }
                }
                mView.showCheckInStatusInView(isCheckedIn);
                mView.updateAlarm(isCheckedIn, mPreferences.getNotificationTime(), mPreferences.isNotificationAllowed());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void setNewCheckIn(CheckIn checkIn, Date date) {
        if (lackInternetConnection()) {
            mView.showConnectionErrorInView();
            return;
        }
        SimpleDateFormat mDateFormat = new SimpleDateFormat("dd_MM_yyyy", Locale.getDefault());
        DatabaseReference reference = mDatabase.getReference(LinksBuilder.buildUrl(MAP, CHECK_IN, mDateFormat.format(date), mPreferences.getId()));
        reference.setValue(checkIn);
        mView.showCheckedInInView(checkIn.getType());
        mView.updateAlarm(true, mPreferences.getNotificationTime(), mPreferences.isNotificationAllowed());
    }

    @Override
    public void sendMessageLateToMainChat(final String messageText) {
        final DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, MAIN_CHAT_ID));
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Chat mainChat = dataSnapshot.getValue(Chat.class);
                DatabaseReference chatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, MAIN_CHAT_ID)).push();
                String id = chatRef.getKey();
                Message message = createMessage(messageText);
                message.setId(id);
                assert mainChat != null;
                DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, MESSAGES, MAIN_CHAT_ID, message.getId()));
                messageRef.setValue(message);
                for (String memberId : mainChat.getMemberIds()) {
                    DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, memberId, MAIN_CHAT_ID));
                    memberRef.setValue(message);
                    setLastMessageStatus(memberId, MAIN_CHAT_ID, true);
                }
                setLastMessageStatus(mPreferences.getId(), MAIN_CHAT_ID, false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private Message createMessage(String messageText) {
        Message.Builder builder = new Message.Builder();
        return builder.setChatId(MAIN_CHAT_ID)
                .setSenderId(mPreferences.getId())
                .setText(messageText)
                .setType("user")
                .setTimeStamp(DateUtil.getStringTime())
                .create();
    }

    private void setLastMessageStatus(String userId, String chatId, boolean isNotRead) {
        DatabaseReference messageRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_NEW_MESSAGES, userId, chatId));
        messageRef.setValue(isNotRead);
    }

    private boolean lackInternetConnection() {
        boolean isConnected;

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo == null || !networkInfo.isConnectedOrConnecting());

        return isConnected;
    }
}
