package io.rolique.cameralibrary.screens.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.rolique.cameralibrary.rx.RxTransformers;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

class CameraPresenter implements CameraContract.Presenter {

    private CameraContract.View mView;
    private CompositeDisposable mCompositeDisposable;

    CameraPresenter(CameraContract.View view) {
        mView = view;
        mCompositeDisposable = new CompositeDisposable();
        mView.setPresenter(CameraPresenter.this);
    }

    @Override
    public void createVideoPreview(final File video, final File previewFile, final int screenWidth, final int screenHeight) {
        Disposable disposable = Single.fromCallable(
                new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        Bitmap thumb = ThumbnailUtils.createVideoThumbnail(video.getPath(),
                                MediaStore.Images.Thumbnails.MINI_KIND);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        thumb.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        return saveToFile(stream.toByteArray(), previewFile);
                    }
                }).compose(RxTransformers.<File> applySingleSchedulers())
                .subscribeWith(new DisposableSingleObserver<File>() {
                                   @Override
                                   public void onSuccess(File file) {
                                       mView.showSavedVideoInView(video, file, screenHeight, screenWidth);
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
    public void savePictureToFile(final byte[] data, final File pictureFile, final int screenWidth, final int screenHeight, final boolean isFrontOrientation, final int orientation) {
        Disposable disposable = Single.fromCallable(
                new Callable<File>() {
                    @Override
                    public File call() throws Exception {
                        Bitmap bm = transformBitmap(data, screenWidth, screenHeight, isFrontOrientation, orientation);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                        return saveToFile(stream.toByteArray(), pictureFile);
                    }
                })
                .compose(RxTransformers.<File> applySingleSchedulers())
                .subscribeWith(new DisposableSingleObserver<File>() {
                                   @Override
                                   public void onSuccess(File file) {
                                       mView.showSavedPictureInView(file, screenHeight, screenWidth);
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

    private Bitmap transformBitmap(byte[] data, int screenWidth, int screenHeight, boolean isFrontOrientation, int orientation) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0, options);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, screenWidth, screenHeight, true);
        int w = scaled.getWidth();
        int h = scaled.getHeight();
        Matrix mtx = new Matrix();
        if (isFrontOrientation) {
            if (orientation == 0 || orientation == 180)
                mtx.postRotate(orientation - 90);
            else
                mtx.postRotate(orientation + 90);
        } else
            mtx.postRotate(orientation + 90);
        return Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
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

    @Override
    public void removeFile(File file) {
        file.delete();
    }

//    @Override
//    public void savePictureToFile(final byte[] data, final File pictureFile, final int screenWidth, final int screenHeight, final int orientation) {
//        Disposable disposable = Single.fromCallable(
//                new Callable<File>() {
//                    @Override
//                    public File call() throws Exception {
//
//                        try (FileOutputStream outputStream = new FileOutputStream(pictureFile)) {
//                            outputStream.write(stream.toByteArray());
//                            outputStream.close();
//                        }
//                        return pictureFile;
//                    }
//                })
//                .compose(RxTransformers.<File> applySingleSchedulers())
//                .subscribeWith(new DisposableSingleObserver<File>() {
//                                   @Override
//                                   public void onSuccess(File file) {
//                                       mView.showSavedPictureInView(file, screenHeight, screenWidth);
//                                   }
//
//                                   @Override
//                                   public void onError(Throwable throwable) {
//                                       Timber.d(throwable);
//                                       mView.showErrorFileSaving(throwable);
//                                   }
//                               }
//
//                );
//        mCompositeDisposable.add(disposable);
//    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        mCompositeDisposable.clear();
    }
}
