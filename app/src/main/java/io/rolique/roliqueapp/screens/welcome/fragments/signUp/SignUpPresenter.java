package io.rolique.roliqueapp.screens.welcome.fragments.signUp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.ChatMessage;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

final class SignUpPresenter implements SignUpContract.Presenter, FirebaseValues {

    private final SignUpFragment mView;

    final FirebaseAuth mAuth;
    final FirebaseDatabase mDatabase;
    final RoliqueApplicationPreferences mPreferences;

    @Inject
    SignUpPresenter(RoliqueApplicationPreferences preferences,
                    SignUpFragment view,
                    FirebaseAuth auth,
                    FirebaseDatabase database) {
        mPreferences = preferences;
        mView = view;
        mAuth = auth;
        mDatabase = database;
    }

    @Override
    public void uploadImage(@NonNull Bitmap bitmap, final String email, final String password, final String firstName, final String lastName, final Activity activity) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(String.format("%s.jpg", new Date().getTime()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
                signUp(email, password, firstName, lastName, downloadUrl, activity);
            }
        });
    }

    private void signUp(final String email, String password, final String firstName, final String lastName, final String imageUrl, Activity activity) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveSignUpCredentials(mAuth.getCurrentUser().getUid(), email, firstName, lastName, imageUrl);
                        } else {
                            task.getException().printStackTrace();
                            mView.showLoginError(task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveSignUpCredentials(String userId, String email, String firstName, String lastName, String imageUrl) {
        User user = new User(userId, email, firstName, lastName, imageUrl);
        mPreferences.logIn(user);
        DatabaseReference ref = mDatabase.getReference(AUTH_USER);
        DatabaseReference myRef = ref.child(userId);

        myRef.setValue(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                Timber.d(databaseReference.getKey());
            }
        });

        registerInChats(userId);
    }

    private void registerInChats(final String userId) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setEmpty();
        DatabaseReference memberRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, USER_CHAT, userId, "main"));
        memberRef.setValue(chatMessage);

        final DatabaseReference mainChatRef = mDatabase.getReference(LinksBuilder.buildUrl(CHAT, CHATS, "main"));
        mainChatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Chat main = dataSnapshot.getValue(Chat.class);
                main.getMemberIds().add(userId);
                mainChatRef.setValue(main);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        mView.showLoginInView();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }
}
