package io.rolique.roliqueapp;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import io.rolique.roliqueapp.data.firebaseData.FirebaseValues;
import io.rolique.roliqueapp.data.model.User;
import io.rolique.roliqueapp.util.LinksBuilder;

/**
 * Created by Volodymyr Oleshkevych on 9/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

@Singleton
public class RoliqueAppUsers implements FirebaseValues {

    private List<User> mUsers;
    private final FirebaseDatabase mDatabase;
    private DatabaseReference mReference;

    public RoliqueAppUsers(FirebaseAuth auth, final FirebaseDatabase database) {
        mDatabase = database;
        mUsers = new ArrayList<>();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) mUsers.clear();
                else setUsersListener();
            }
        });
    }

    public List<User> getUsers() {
        return mUsers;
    }

    private void setUsersListener() {
        mReference = mDatabase.getReference(AUTH_USER);
        mReference.addChildEventListener(mChildEventListener);
    }

    ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            DatabaseReference userRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH_USER, dataSnapshot.getKey()));
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    for (int i = 0; i < mUsers.size(); i++)
                        if (mUsers.get(i).getId().equals(user.getId())) {
                            mUsers.set(i, user);
                            return;
                        }
                    mUsers.add(user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            DatabaseReference userRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH_USER, dataSnapshot.getKey()));
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    for (int i = 0; i < mUsers.size(); i++)
                        if (mUsers.get(i).getId().equals(user.getId())) {
                            mUsers.set(i, user);
                            break;
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            DatabaseReference userRef = mDatabase.getReference(LinksBuilder.buildUrl(AUTH_USER, dataSnapshot.getKey()));
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (User user : mUsers)
                        if (user.getId().equals(dataSnapshot.getKey())) {
                            mUsers.remove(user);
                            return;
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    @Override
    protected void finalize() throws Throwable {
        mReference.removeEventListener(mChildEventListener);
        super.finalize();
    }
}
