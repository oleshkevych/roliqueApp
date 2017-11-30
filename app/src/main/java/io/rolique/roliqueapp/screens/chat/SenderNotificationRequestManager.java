package io.rolique.roliqueapp.screens.chat;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.data.model.Message;
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
public class SenderNotificationRequestManager {

    private final String API_KEY_ID = "key=AAAA0TfL4rg:APA91bEJGV84uesk-w8UeEOzpHe8HQiLNsMthHdFM5cKLBWWwAt2cSJDz64oeap9H02FKKpplorfCGfQDpQ5GW6hvKP65-FZKL-jBUgsEpxDPHG3Vr9o4_yrzTwi0PdlzDiUTzsEa-Ng";
    private final String API_KYE_FCM = "key=AIzaSyCFlU_B6O-DVTyd_DAHqq1op-dJdeLJmuA";
    private static final String URL = "https://fcm.googleapis.com/fcm/send";

    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_VALUE_JSON = "application/json";
    private static final String HEADER_NAME_CACHE = "cache-control";
    private static final String HEADER_NAME_CACHE_VALUE = "no-cache";
    private static final String AUTHORIZATION = "authorization";
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse(HEADER_VALUE_JSON);

    private static final int CONNECT_TIMEOUT = 10;
    private static final int WRITE_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 60;

    private final ConnectivityManager mConnectivityManager;
    private final OkHttpClient mOkHttpClient;
    private final Chat mChat;
    private final String mUserName;

    public SenderNotificationRequestManager(Context context, Chat chat, String userName) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mOkHttpClient = buildClient();
        mChat = chat;
        mUserName = userName;
    }

    private OkHttpClient buildClient() {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(createAuthInterceptor());
        return builder.build();
    }

    private Interceptor createAuthInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder();
                builder.header(HEADER_NAME_CONTENT_TYPE, HEADER_VALUE_JSON);
                builder.header(AUTHORIZATION, API_KYE_FCM);
                builder.header(HEADER_NAME_CACHE, HEADER_NAME_CACHE_VALUE);

                request = builder.build();
                Timber.d(request.toString());
                Timber.d(request.headers().toString());
                Response response = chain.proceed(request);

                switch (response.code()) {
                    case 500:
                    case 503:
                    case 401:
                        Timber.e("Error: " + response.body().string());
                }
                return response;
            }
        };
    }

    void sendMessage(Message message) {
        try {
            Timber.d(createPOST(URL, message));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String createPOST(String url, Message message) throws Exception {
        if (isThereInternetConnection()) {

            Request request = buildPOSTRequest(url, createJson(message));
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            return body.string();
        } else {
            throw new IOException("There is no internet connection.");
        }
    }

    private String createJson(Message message) {
        return String.format("{" +
                "            \"to\":\"/topics/%s\"," +
                "            \"priority\": \"high\"," +
                "            \"notification\": {" +
                "                \"body\": \"%s : %s\"," +
                "                \"title\": \"%s\"," +
                "                \"sound\": \"default\"," +
                "                \"chatId\": \"%s\"}" +
                "}", mChat.getId(), mUserName, getMessage(message), mChat.getTitle(), mChat.getId());
    }

    private String getMessage(Message message) {
        if (message.getText() == null || message.getText().isEmpty())
            if (message.getMedias() != null) return "media message";
        return message.getText();
    }

    private Request buildPOSTRequest(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        return new Request.Builder()
                .url(url)
                .post(body)
                .build();
    }

    private boolean isThereInternetConnection() {
        boolean isConnected;

        NetworkInfo networkInfo = mConnectivityManager.getActiveNetworkInfo();
        isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());

        return isConnected;
    }
}
