package io.rolique.roliqueapp.util;

import android.support.annotation.Nullable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

/**
 * Created by Volodymyr Oleshkevych on 4/14/2017.
 * Copyright (c) 2017, Rolique. All rights reserved.
 */

public final class DateUtil {
    private static final String DATE_FORMATE = "yyyy-MM-dd HH:mm:ss ZZZZZ";

    public static Date transformDate(String dateInString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMATE, Locale.getDefault());
            if(dateInString.lastIndexOf('+') > 0)
                dateInString = dateInString.substring(0, dateInString.lastIndexOf('+') - 1);
            return sdf.parse(dateInString);
        } catch (Exception e) {
            Timber.e(e);
            return new Date(0);
        }
    }

    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return isSameDay(cal1, cal2);
    }

    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    public static boolean isToday(Date date) {
        return isSameDay(date, Calendar.getInstance().getTime());
    }

    public static boolean isToday(Calendar cal) {
        return isSameDay(cal, Calendar.getInstance());
    }

    public static String getStringDate(int date) {
        if (date < 10) return String.format("%s%s", 0, date);
        return String.valueOf(date);
    }
}
