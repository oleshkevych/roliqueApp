package io.rolique.roliqueapp.services.messageService;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 27.11.17.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class MessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Timber.e(TAG+ " From: " + remoteMessage.getFrom());
        Timber.e(TAG +" Notification Message Body: " + remoteMessage.getNotification().getBody());
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
        Timber.e(TAG+ " Sent: " + s);
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        Timber.e(TAG+ " Sent: " + s + " " + e.getMessage());
    }

    public MessagingService() {
        super();
        Timber.e(TAG+ " START");
    }
}