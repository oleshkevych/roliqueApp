package io.rolique.roliqueapp.services.alarmNotification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Calendar;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.screens.navigation.NavigationActivity;
import io.rolique.roliqueapp.services.remaidReceiver.RemindReceiver;
import io.rolique.roliqueapp.util.AlarmBuilder;

import static android.app.Notification.DEFAULT_SOUND;

/**
 * Created by Volodymyr Oleshkevych on 11/10/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public class NotificationAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final int NOTIFICATION_ID = 38;
        AlarmBuilder.resetAlarm(context);
        Calendar calNow = Calendar.getInstance();
        if (calNow.get(Calendar.DAY_OF_WEEK) == 0 ||
                calNow.get(Calendar.DAY_OF_WEEK) == 7)
            return;

        Intent intentAction = new Intent(context, RemindReceiver.class);
        intentAction.putExtra(context.getString(R.string.extra_notification_id), NOTIFICATION_ID);
        PendingIntent pIntentButton = PendingIntent.getBroadcast(context, 1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent pIntent = PendingIntent.getActivity(context, 0, NavigationActivity.startIntent(context, true), PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context)
                .setSmallIcon(R.drawable.ic_notification_round)
                .setTicker(context.getString(R.string.alarm_receiver_message))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.alarm_receiver_message))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setDefaults(DEFAULT_SOUND)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOngoing(true)
                .addAction(R.drawable.ic_add_alarm_black_24dp, context.getString(R.string.alarm_receiver_remind_button), pIntentButton);

        NotificationManager notificationmanager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationmanager != null;
        notificationmanager.notify(NOTIFICATION_ID, builder.build());
    }
}