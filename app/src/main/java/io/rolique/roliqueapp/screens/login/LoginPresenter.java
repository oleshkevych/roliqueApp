package io.rolique.roliqueapp.screens.login;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;

import javax.inject.Inject;

import io.rolique.roliqueapp.RoliqueApplicationPreferences;
import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.User;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 8/16/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

final class LoginPresenter implements LoginContract.Presenter, FirebaseValues {

    private final LoginActivity mView;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    final RoliqueApplicationPreferences mPreferences;
    final Context mContext;
    Query mQuery;

    @Inject
    LoginPresenter(Context context, RoliqueApplicationPreferences preferences, LoginActivity view) {
        mPreferences = preferences;
        mContext = context;
        mView = view;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    @Override
    public void start() {
        if(mQuery != null)
            mQuery.addValueEventListener(mListener);
    }

    @Override
    public void stop() {
        if(mQuery != null)
            mQuery.removeEventListener(mListener);
    }

    @Override
    public void uploadImage(@NonNull Bitmap bitmap, final String email, final String password, final String firstName, final String lastName) {
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
                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                signUp(email, password, firstName, lastName, downloadUrl);
            }
        });
    }

    @Override
    public void signIn(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mView, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveSignInCredentials(mAuth.getCurrentUser().getUid());
                        } else {
                            task.getException().printStackTrace();
                            mView.showLoginError();
                        }
                    }
                });
    }

    private void saveSignInCredentials(final String uid) {
        mQuery = mDatabase.getReference(AUTH_USER).child(uid);
        mQuery.addValueEventListener(mListener);
    }

    private ValueEventListener mListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            User user = dataSnapshot.getValue(User.class);
            Timber.d(user.toString());
            mPreferences.logIn(user);
            mView.showLoginInView();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };


    private void signUp(final String email, String password, final String firstName, final String lastName, final String imageUrl) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mView, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveSignUpCredentials(mAuth.getCurrentUser().getUid(), email, firstName, lastName, imageUrl);
                        } else {
                            task.getException().printStackTrace();
                            mView.showLoginError();
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
        mView.showLoginInView();
    }
}
