package io.rolique.cameralibrary.screens.camera;

import java.io.File;


/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

interface CameraContract {
    interface View<Presenter> {
        void showSavedPictureInView(File file, int height, int width);
        void showSavedVideoInView(File video, File preview, int height, int width);
        void showErrorFileSaving(Throwable t);
        void setPresenter(CameraPresenter presenter);
    }

    interface Presenter {
        void createVideoPreview(File video, File preview, int screenWidth, int screenHeight);
        void savePictureToFile(byte[] data, File pictureFile, int screenWidth, int screenHeight, boolean isFrontOrientation, int orientation);
//        void removeFile(String file);
        void start();
        void stop();
    }
}
