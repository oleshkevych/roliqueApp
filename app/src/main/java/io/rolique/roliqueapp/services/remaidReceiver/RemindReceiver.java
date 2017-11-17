package io.rolique.roliqueapp.services.remaidReceiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.util.AlarmBuilder;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/17/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class RemindReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = intent.getIntExtra(context.getString(R.string.extra_notification_id), 0);
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(notificationId);

        Toast.makeText(context,"Remind activated", Toast.LENGTH_SHORT).show();
        AlarmBuilder.setRemindAlarm(context, false);
    }
}