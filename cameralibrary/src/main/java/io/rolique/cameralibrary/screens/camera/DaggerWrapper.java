package io.rolique.cameralibrary.screens.camera;

/**
 * Created by Volodymyr Oleshkevych on 9/21/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class DaggerWrapper {

    private static CameraComponent mComponent;

    public static CameraComponent getComponent(CameraContract.View view) {
        if (mComponent == null) {
            initComponent(view);
        }
        return mComponent;
    }

    private static void initComponent(CameraContract.View view) {
        mComponent = DaggerCameraComponent
                .builder()
                .cameraPresenterModule(new CameraPresenterModule(view))
                .build();
    }
}
