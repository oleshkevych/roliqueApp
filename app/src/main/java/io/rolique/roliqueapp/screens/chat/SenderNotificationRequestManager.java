package io.rolique.roliqueapp.screens.chat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
import io.rolique.roliqueapp.util.RequestBuilder;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/28/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class SenderNotificationRequestManager {

    private final String API_KYE_FCM = "key=AIzaSyCFlU_B6O-DVTyd_DAHqq1op-dJdeLJmuA";
    private static final String URL = "https://fcm.googleapis.com/fcm/send";
    private static final String HEADER_VALUE_JSON = "application/json";

    private final Chat mChat;
    private final String mUserName;
    private final RequestBuilder mRequestBuilder;

    SenderNotificationRequestManager(Context context, Chat chat, String userName) {
        mChat = chat;
        mUserName = userName;
        mRequestBuilder = new RequestBuilder(context, API_KYE_FCM, HEADER_VALUE_JSON);
    }

    void sendMessage(Message message) {
        try {
            Timber.d(mRequestBuilder.sendPOST(URL, createJson(message)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createJson(Message message) {
        return String.format("{" +
                "\"to\":\"/topics/%s\"," +
                "\"priority\": \"high\"," +
                "\"notification\": {" +
                "\"body\": \"%s : %s\"," +
                "\"title\": \"%s\"," +
                "\"sound\": \"default\"," +
                "\"chatId\": \"%s\"}" +
                "}", mChat.getId(), mUserName, getMessage(message), mChat.getTitle(), mChat.getId());
    }

    private String getMessage(Message message) {
        if (message.getText() == null || message.getText().isEmpty())
            if (message.getMedias() != null) return "media message";
        return message.getText();
    }
}
