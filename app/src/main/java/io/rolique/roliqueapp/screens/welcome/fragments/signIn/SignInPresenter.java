package io.rolique.roliqueapp.screens.welcome.fragments.signIn;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

final class SignInPresenter implements SignInContract.Presenter, FirebaseValues {

    private final SignInFragment mView;

    FirebaseAuth mAuth;
    FirebaseDatabase mDatabase;
    final RoliqueApplicationPreferences mPreferences;
    Query mQuery;

    @Inject
    SignInPresenter(RoliqueApplicationPreferences preferences,
                    SignInFragment view,
                    FirebaseAuth auth,
                    FirebaseDatabase database) {
        mPreferences = preferences;
        mView = view;
        mAuth = auth;
        mDatabase = database;
    }

    @Override
    public void start() {
        if (mQuery != null)
            mQuery.addValueEventListener(mListener);
    }

    @Override
    public void stop() {
        if (mQuery != null)
            mQuery.removeEventListener(mListener);
    }

    @Override
    public void signIn(final String email, final String password, Activity activity) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            saveSignInCredentials(mAuth.getCurrentUser().getUid());
                        } else {
                            task.getException().printStackTrace();
                            mView.showLoginError(task.getException().getMessage());
                        }
                    }
                });
    }

    @Override
    public void resetPassMail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mView.showEmailSentInView(task.isSuccessful());
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
}
