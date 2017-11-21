package io.rolique.roliqueapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;

import io.rolique.roliqueapp.services.notification.NotificationService;

/**
 * Created by Volodymyr Oleshkevych on 11/10/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class AlarmBuilder {

    private final static int RQS_1 = 1;
    private final static int RQS_2 = 2;

    public static void resetAlarm(Context context) {
        setAlarm(context, Calendar.getInstance(), true, false);
    }

    public static void setAlarm(Context context, String time, boolean isAlreadyChecked, boolean isCancel) {
        String[] strings = time.split(" ");
        Calendar calSet = Calendar.getInstance();
        calSet.set(Calendar.HOUR_OF_DAY, Integer.valueOf(strings[0]));
        calSet.set(Calendar.MINUTE, Integer.valueOf(strings[1]));
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);
        setAlarm(context, calSet, isAlreadyChecked, isCancel);
        setRemindAlarm(context, true);
    }

    private static void setAlarm(Context context, Calendar calSet, boolean isAlreadyChecked, boolean isCancel) {
        Calendar calNow = Calendar.getInstance();
        if (calSet.compareTo(calNow) <= 0 || isAlreadyChecked)
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);

        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, RQS_1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
        if (isCancel) return;
        alarmManager.set(AlarmManager.RTC_WAKEUP, calSet.getTimeInMillis(), pendingIntent);
    }

    public static void setRemindAlarm(Context context, boolean isCancel) {
        Calendar calNow = Calendar.getInstance();

        Intent intent = new Intent(context, NotificationService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, RQS_2, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
        if (isCancel) return;
        alarmManager.set(AlarmManager.RTC_WAKEUP, calNow.getTimeInMillis() + 10 * 60 * 1000, pendingIntent);
    }
}
