package io.rolique.roliqueapp.screens.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.rolique.roliqueapp.rx.RxTransformers;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class CameraPresenter implements CameraContract.Presenter {

    private CameraContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    @Inject
    CameraPresenter(CameraContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
    }

    @Override
    public void savePictureToFile(final byte[] data, final File pictureFile, final int screenWidth, final int screenHeight, final boolean isFrontOrientation) {
        Disposable disposable = Single.fromCallable(
                new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        Bitmap bm = transformBitmap(data, screenWidth, screenHeight, isFrontOrientation);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 70, stream);
                        return saveToFile(stream.toByteArray(), pictureFile);
                    }
                })
                .compose(RxTransformers.<File> applySingleSchedulers())
                .subscribeWith(new DisposableSingleObserver<File>() {
                                   @Override
                                   public void onSuccess(File file) {
                                       mView.showSavedFileInView(file, screenHeight, screenWidth);
                                   }

                                   @Override
                                   public void onError(Throwable throwable) {
                                       Timber.d(throwable);
                                       mView.showErrorFileSaving(throwable);
                                   }
                               }

                );
        mCompositeDisposable.add(disposable);
    }

    private File saveToFile(byte[] byteArray, File file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArray);
            fos.close();
        } catch (Exception e) {
            Timber.e(e);
        }
        return file;
    }

    private Bitmap transformBitmap(byte[] data, int screenWidth, int screenHeight, boolean isFrontOrientation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0, options);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenHeight / 2, screenWidth / 2, true);
        int w = scaled.getWidth();
        int h = scaled.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(isFrontOrientation ? -90 : 90);
        return Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
    }

    @Override
    public void removeFile(String path) {
        new File(path).delete();
    }

    @Override
    public void savePictureToFile(final byte[] data, final File pictureFile, final int screenWidth, final int screenHeight) {
        Disposable disposable = Single.fromCallable(
                new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        try (FileOutputStream outputStream = new FileOutputStream(pictureFile)) {
                            outputStream.write(data);
                        }
                        return pictureFile;
                    }
                })
                .compose(RxTransformers.<File> applySingleSchedulers())
                .subscribeWith(new DisposableSingleObserver<File>() {
                                   @Override
                                   public void onSuccess(File file) {
                                       mView.showSavedFileInView(file, screenHeight, screenWidth);
                                   }

                                   @Override
                                   public void onError(Throwable throwable) {
                                       Timber.d(throwable);
                                       mView.showErrorFileSaving(throwable);
                                   }
                               }

                );
        mCompositeDisposable.add(disposable);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        mCompositeDisposable.clear();
    }
}
