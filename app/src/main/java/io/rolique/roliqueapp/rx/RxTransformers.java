package io.rolique.roliqueapp.rx;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Volodymyr Oleshkevych on 9/20/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class RxTransformers {

    public static <T> SingleTransformer<T, T> applySingleSchedulers() {
        return new SingleTransformer<T, T>() {
            @Override
            public SingleSource<T> apply(Single<T> upstream) {
                return upstream.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }
}
