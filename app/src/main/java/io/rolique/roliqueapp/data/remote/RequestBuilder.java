package io.rolique.roliqueapp.data.remote;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;

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
 * Created by Volodymyr Oleshkevych on 11/30/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
class RequestBuilder {

    private static final String HEADER_NAME_CONTENT_TYPE = "Content-Type";

    private static final String HEADER_NAME_CACHE = "cache-control";
    private static final String HEADER_NAME_CACHE_VALUE = "no-cache";
    private static final String AUTHORIZATION = "authorization";

    private static final int CONNECT_TIMEOUT = 10;
    private static final int WRITE_TIMEOUT = 10;
    private static final int READ_TIMEOUT = 60;

    private final ConnectivityManager mConnectivityManager;
    private final OkHttpClient mOkHttpClient;
    private final String mIdKey;
    private final String mHeaderValue;

    RequestBuilder(Context context, String idKey, String headerValue) {
        mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        mOkHttpClient = buildClient();
        mIdKey = idKey;
        mHeaderValue = headerValue;
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
            public Response intercept(@NonNull Chain chain) throws IOException {
                Request request = chain.request();
                Request.Builder builder = request.newBuilder();
                builder.header(HEADER_NAME_CONTENT_TYPE, mHeaderValue);
                builder.header(AUTHORIZATION, mIdKey);
                builder.header(HEADER_NAME_CACHE, HEADER_NAME_CACHE_VALUE);

                request = builder.build();
                Timber.d(request.toString());
                Timber.d(request.headers().toString());
                Response response = chain.proceed(request);

                Timber.d("Code: " + response.code());
                if (response.code() != 200) {
                    Timber.e("Error Message: " + response.message());
                    throw new IOException("Error");
                }
                return response;
            }
        };
    }

    String sendPOST(String url, String json) throws Exception {
        if (isThereInternetConnection()) {
            Timber.d(json);
            Request request = buildPOSTRequest(url, json);
            Response response = mOkHttpClient.newCall(request).execute();
            ResponseBody body = response.body();
            return body.string();
        } else {
            throw new IOException("There is no internet connection.");
        }
    }

    private Request buildPOSTRequest(String url, String json) throws Exception {
        RequestBody body = RequestBody.create(MediaType.parse(mHeaderValue), json);
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
