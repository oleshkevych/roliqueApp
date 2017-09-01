package io.rolique.roliqueapp.util;

/**
 * Created by Volodymyr Oleshkevych on 9/1/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class LinksBuilder {

    public static String buildUrl(String url, String... params) {
        for (String param : params)
            url = String.format("%s/%s", url, param);
        return url;
    }
}
