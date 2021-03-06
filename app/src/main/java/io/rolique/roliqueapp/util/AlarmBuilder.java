package io.rolique.roliqueapp.util;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.Calendar;
import java.util.Date;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.services.bootReceiver.SampleBootReceiver;
import io.rolique.roliqueapp.services.jobScheduler.JobSchedulerService;
import io.rolique.roliqueapp.services.notification.NotificationService;
import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 11/10/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class AlarmBuilder {

    public static void resetAlarm(Context context) {
        setAlarm(context, false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setRemindAlarm(context, true);
        } else {
            setRemindAlarm1(context, true);
        }
    }

    public static void setAlarm(Context context, boolean isAlreadyChecked) {
        SharedPreferences preferences = context.getApplicationContext()
                .getSharedPreferences(context.getString(R.string.extra_shared_preferences), Context.MODE_PRIVATE);
        String time = preferences.getString(context.getString(R.string.extra_shared_preferences_notification_time), "10 45");
        Boolean isAllowed = preferences.getBoolean(context.getString(R.string.extra_shared_preferences_notification_allowed), true);

        String[] strings = time.split(" ");
        Calendar calSet = Calendar.getInstance();
        calSet.set(Calendar.HOUR_OF_DAY, Integer.valueOf(strings[0]));
        calSet.set(Calendar.MINUTE, Integer.valueOf(strings[1]));
        calSet.set(Calendar.SECOND, 0);
        calSet.set(Calendar.MILLISECOND, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            setUpJobScheduler(context, calSet, isAlreadyChecked, !isAllowed);
        else
            setAlarm1(context, calSet, isAlreadyChecked, !isAllowed);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void setUpJobScheduler(Context context, Calendar calSet, boolean isAlreadyChecked, boolean isCancel) {
        Calendar calNow = Calendar.getInstance();
        if (calSet.compareTo(calNow) <= 0 || isAlreadyChecked)
            //Today Set time passed, count to tomorrow
            calSet.add(Calendar.DATE, 1);
        if (isCancel) {
            cancelAllJobs(context);
            if (!isMyServiceRunning(context)) {
                Intent serviceIntent = new Intent(context, JobSchedulerService.class);
                context.stopService(serviceIntent);
            }
        } else scheduleJob(context, calSet);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static boolean isMyServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (JobSchedulerService.class.getName().equals(service.service.getClassName())) {
                Timber.e("isMyServiceRunning? true");
                return true;
            }
        }
        Timber.e("isMyServiceRunning false");
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void scheduleJob(Context context, Calendar calSet) {
        if (!isMyServiceRunning(context)) {
            Intent serviceIntent = new Intent(context, JobSchedulerService.class);
            context.startService(serviceIntent);
        }
        cancelAllJobs(context);
        ComponentName mServiceComponent = new ComponentName(context, JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(1, mServiceComponent);

        builder.setMinimumLatency(calSet.getTimeInMillis() - new Date().getTime() - 1000);
        builder.setOverrideDeadline(calSet.getTimeInMillis() - new Date().getTime());

        // Schedule job
        Timber.d("Scheduling job");
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert tm != null;
        tm.schedule(builder.build());
    }

    /**
     * Executed when user clicks on CANCEL ALL.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void cancelAllJobs(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert tm != null;
        tm.cancelAll();
    }

    /**
     * Executed when user clicks on FINISH LAST TASK.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static void finishJob(Context context, int id) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert jobScheduler != null;
        jobScheduler.cancel(id);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static void setRemindAlarm(Context context, boolean isCancel) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            setRemindAlarm1(context, isCancel);
            return;
        }
        if (isCancel) {
            finishJob(context, 2);
            return;
        }
        ComponentName mServiceComponent = new ComponentName(context, JobSchedulerService.class);
        JobInfo.Builder builder = new JobInfo.Builder(2, mServiceComponent);

        builder.setMinimumLatency(10 * 60 * 1000 - 1000);
        builder.setOverrideDeadline(10 * 60 * 1000);

        // Schedule job
        Timber.d("Scheduling remind job");
        JobScheduler tm = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert tm != null;
        tm.schedule(builder.build());
    }

    private static void setAlarm1(Context context, Calendar calSet, boolean isAlreadyChecked, boolean isCancel) {
        setUpAlarmLifecycle(context, isCancel);
        int RQS_1 = 1;
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

    private static void setUpAlarmLifecycle(Context context, boolean isCancel) {
        ComponentName receiver = new ComponentName(context, SampleBootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                isCancel ?
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED :
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public static void setRemindAlarm1(Context context, boolean isCancel) {
        int RQS_2 = 2;
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
