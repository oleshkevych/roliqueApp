package io.rolique.roliqueapp.services.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;
import io.rolique.roliqueapp.services.remindService.RemindService;
import io.rolique.roliqueapp.util.AlarmBuilder;

import static android.app.Notification.DEFAULT_SOUND;

/**
 * Created by Volodymyr Oleshkevych on 11/21/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class NotificationService extends Service {

    public static final int NOTIFICATION_ID = 38;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String NOTIFICATION_CHANNEL = "MY_NOTIFICATION_CHANNEL";
        AlarmBuilder.resetAlarm(NotificationService.this);
//        Calendar calNow = Calendar.getInstance();
//        if (calNow.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
//                calNow.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
//            return START_NOT_STICKY;

        Intent intentAction = new Intent(NotificationService.this, RemindService.class);
        intentAction.putExtra(getString(R.string.extra_notification_id), NOTIFICATION_ID);
        PendingIntent pIntentButton = PendingIntent.getService(NotificationService.this, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);


        PendingIntent pIntent = PendingIntent.getActivity(NotificationService.this, 0, NavigationActivity.startIntent(NotificationService.this, true), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(NotificationService.this, NOTIFICATION_CHANNEL)
                .setSmallIcon(R.drawable.ic_notification_round)
                .setTicker(getString(R.string.alarm_receiver_message))
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.alarm_receiver_message))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setDefaults(DEFAULT_SOUND)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_add_alarm_black_24dp, getString(R.string.alarm_receiver_remind_button), pIntentButton);

        NotificationManager notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationmanager != null;
        notificationmanager.notify(NOTIFICATION_ID, builder.build());
        stopSelf();
        return START_NOT_STICKY;
    }
}
