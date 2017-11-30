package io.rolique.roliqueapp.screens.editChat;

import android.content.Context;

import java.util.List;

import io.rolique.roliqueapp.data.model.Chat;
import io.rolique.roliqueapp.util.RequestBuilder;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/30/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class SubscribeManager {

    private static final String API_KEY_ID = "key=AAAA0TfL4rg:APA91bEJGV84uesk-w8UeEOzpHe8HQiLNsMthHdFM5cKLBWWwAt2cSJDz64oeap9H02FKKpplorfCGfQDpQ5GW6hvKP65-FZKL-jBUgsEpxDPHG3Vr9o4_yrzTwi0PdlzDiUTzsEa-Ng";
    private static final String URL = "https://iid.googleapis.com/iid/v1:";
    private static final String HEADER_VALUE_X_FORM = "application/x-www-form-urlencoded";

    private final Chat mChat;
    private final RequestBuilder mRequestBuilder;

    SubscribeManager(Context context, Chat chat) {
        mChat = chat;
        mRequestBuilder = new RequestBuilder(context, API_KEY_ID, HEADER_VALUE_X_FORM);
    }

    boolean sendSubscribeRequest(List<String> tokens, boolean isSubscribe) {
        try {
            String url = URL + (isSubscribe ? "batchAdd" : "batchRemove");
            Timber.d(mRequestBuilder.sendPOST(url, createJson(tokens)));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String createJson(List<String> tokens) {
        return String.format("{" +
                "\"to\":\"/topics/%s\"," +
                "\"registration_tokens\":%s" +
                "}", mChat.getId(), getJsonArray(tokens));
    }

    private String getJsonArray(List<String> tokens) {
        String jsonArray = "[";
        for (int i = 0; i < tokens.size(); i++)
            if (i == 0) jsonArray = jsonArray + "\"" + tokens.get(i) + "\"";
            else jsonArray = jsonArray + ",\"" + tokens.get(i) + "\"";
        jsonArray = jsonArray + "]";
        return jsonArray;
    }
}
