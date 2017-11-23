package io.rolique.roliqueapp.services.bootReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import io.rolique.roliqueapp.R;
import io.rolique.roliqueapp.util.AlarmBuilder;

/**
 * Created by Volodymyr Oleshkevych on 11/22/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */
public class SampleBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Start Boot Broadcast", Toast.LENGTH_LONG).show();

            SharedPreferences sharedPreferences = context
                    .getApplicationContext()
                    .getSharedPreferences(context.getString(R.string.extra_shared_preferences), Context.MODE_PRIVATE);
            String time = sharedPreferences.getString(context.getString(R.string.extra_shared_preferences_notification_time), "10 45");
            boolean isAllowed = sharedPreferences.getBoolean(context.getString(R.string.extra_shared_preferences_notification_allowed), true);

        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmBuilder.setAlarm(context, time, false, !isAllowed);
            Toast.makeText(context, "Reseted Alarm", Toast.LENGTH_LONG).show();
        }
    }
}