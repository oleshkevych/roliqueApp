package io.rolique.roliqueapp.util;

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
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    public static Date transformDate(String dateInString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            return sdf.parse(dateInString);
        } catch (Exception e) {
            Timber.e(e);
            return new Date(0);
        }
    }

    public static boolean isSameDay(String date1, String date2) {
        return isSameDay(transformDate(date1), transformDate(date2));
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

    public static boolean isFirstEarlier(String stringDate1, String stringDate2) {
        Date date1 = transformDate(stringDate1);
        Date date2 = transformDate(stringDate2);
        if (date1 == null || date2 == null) {
            throw new IllegalArgumentException("The dates must not be null");
        }
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        if (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)) {
            if (cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)) {
                if (cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY)) {
                     return cal1.get(Calendar.MINUTE) > cal2.get(Calendar.MINUTE);
                } else return cal1.get(Calendar.HOUR_OF_DAY) > cal2.get(Calendar.HOUR_OF_DAY);
            } else return cal1.get(Calendar.DAY_OF_YEAR) > cal2.get(Calendar.DAY_OF_YEAR);
        } else return cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR);
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

    public static String getStringTime() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        String date = sdf.format(new Date());
        String date1 = date.substring(0, date.length() - 2);
        String date2 = date.substring(date.length() - 2);
        return String.format("%s:%s", date1, date2);
    }

    public static String getStringMessageDate(String timeStamp) {
        Date messageDate = transformDate(timeStamp);
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(new Date());
        if (messageCalendar.get(Calendar.ERA) == currentCalendar.get(Calendar.ERA) &&
                messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
            switch (currentCalendar.get(Calendar.DAY_OF_YEAR) - messageCalendar.get(Calendar.DAY_OF_YEAR)) {
                case 0:
                    String hour = DateUtil.getStringDate(messageCalendar.get(Calendar.HOUR_OF_DAY));
                    String minutes = DateUtil.getStringDate(messageCalendar.get(Calendar.MINUTE));
                    return String.format("%s:%s", hour, minutes);
                case 1:
                    return "Yesterday";
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    return sdf.format(messageDate);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        return sdf.format(messageDate);
    }

    public static String getDetailedStringMessageDate(String timeStamp) {
        Date messageDate = transformDate(timeStamp);
        Calendar messageCalendar = Calendar.getInstance();
        messageCalendar.setTime(messageDate);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(new Date());
        if (messageCalendar.get(Calendar.ERA) == currentCalendar.get(Calendar.ERA) &&
                messageCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
            String hour = DateUtil.getStringDate(messageCalendar.get(Calendar.HOUR_OF_DAY));
            String minutes = DateUtil.getStringDate(messageCalendar.get(Calendar.MINUTE));
            switch (currentCalendar.get(Calendar.DAY_OF_YEAR) - messageCalendar.get(Calendar.DAY_OF_YEAR)) {
                case 0:
                    return String.format("%s:%s", hour, minutes);
                case 1:
                    return String.format("Yesterday %s:%s", hour, minutes);
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
                    return String.format("%s %s:%s", sdf.format(messageDate), hour, minutes);
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd MM yyyy", Locale.getDefault());
        return sdf.format(messageDate);
    }

    public static int getDayOfYear(Date date) {
        if (date == null)
            throw new IllegalArgumentException("The dates must not be null");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    public static int getDayOfWeek(Date date) {
        if (date == null)
            throw new IllegalArgumentException("The dates must not be null");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static int getDayOfMonth(Date date) {
        if (date == null)
            throw new IllegalArgumentException("The dates must not be null");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isLate() {
        Calendar calendar = Calendar.getInstance();
        return ((calendar.get(Calendar.HOUR_OF_DAY) > 10) ||
                (calendar.get(Calendar.HOUR_OF_DAY) == 10 && calendar.get(Calendar.MINUTE) > 47));
    }
}
