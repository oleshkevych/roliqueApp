package io.rolique.roliqueapp.screens.camera;

import java.io.File;

import io.rolique.roliqueapp.screens.BasePresenter;
import io.rolique.roliqueapp.screens.BaseView;


/**
 * Created by Volodymyr Oleshkevych on 5/12/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

interface CameraContract {
    interface View extends BaseView<Presenter> {
        void showSavedFileInView(File file, int height, int width);
        void showErrorFileSaving(Throwable t);
    }

    interface Presenter extends BasePresenter {
        @Deprecated
        void savePictureToFile(byte[] data, File pictureFile, int screenWidth, int screenHeight, boolean isFrontOrientation);
        void savePictureToFile(final byte[] data, final File pictureFile, int screenWidth, int screenHeight);
        void removeFile(String path);
    }
}
