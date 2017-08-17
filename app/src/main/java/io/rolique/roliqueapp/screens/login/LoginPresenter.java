package io.rolique.roliqueapp.screens.login;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

    }

    @Override
    public void stop() {

    }

    @Override
    public void signIn(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(mView, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            task.getException().printStackTrace();
                            mView.showLoginError();
                        } else {
                            saveSignInCredentials(mAuth.getCurrentUser().getUid());
                        }
                    }
                });
    }

    private void saveSignInCredentials(final String uid) {
        mDatabase.getReference(AUTH_USER).child(uid).addValueEventListener(new ValueEventListener() {
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
        });
    }

    @Override
    public void signUp(final String email, String password, final String firstName, final String lastName) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(mView, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            task.getException().printStackTrace();
                            mView.showLoginError();
                        } else {
                            saveSignUpCredentials(mAuth.getCurrentUser().getUid(), email, firstName, lastName);
                        }
                    }
                });
    }

    private void saveSignUpCredentials(String userId, String email, String firstName, String lastName) {
        User user = new User(userId, email, firstName, lastName);
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
