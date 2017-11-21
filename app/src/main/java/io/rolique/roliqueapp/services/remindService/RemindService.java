package io.rolique.roliqueapp.services.remindService;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.util.AlarmBuilder;

/**
 * Created by Volodymyr Oleshkevych on 11/17/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RemindService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        int notificationId = intent.getIntExtra(getString(R.string.extra_notification_id), 0);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notificationId);

        Toast.makeText(RemindService.this, "Remind activated", Toast.LENGTH_SHORT).show();
        AlarmBuilder.setRemindAlarm(RemindService.this, false);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}