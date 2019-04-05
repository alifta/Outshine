package ca.mun.outshine.util;

import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import static java.text.DateFormat.getDateInstance;

public class TimeUtil {

    private static final String TAG = "TimeUtil";

    // create data/time range for reading fitness data
    public static long[] timeRange(int dateAgo, boolean untilNow) {
        // date is the day from today, meaning:
        // 0 -> today, 1 -> one day before or yesterday, 2 -> the day before yesterday
        long startTime;
        long endTime;

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);

        endTime = cal.getTimeInMillis();
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);
        startTime = cal.getTimeInMillis();

        if (dateAgo != 0) {
            cal.add(Calendar.DATE, -dateAgo);
            startTime = cal.getTimeInMillis();
            if (untilNow) {
                cal.add(Calendar.DATE, +dateAgo);
            } else {
                cal.add(Calendar.DATE, +1);
            }
            endTime = cal.getTimeInMillis();
        }

        java.text.DateFormat dateFormat = getDateInstance();

        Log.d(TAG, "Start Time: " + dateFormat.format(startTime)
                + "\tEnd Time: " + dateFormat.format(endTime));

        return new long[]{startTime, endTime};
    }

    // return beginning time (midnight 12 am) of current day in epoch
    public static Date todayMidnight() {
        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, Calendar.AM);

        return cal.getTime();
    }

    // return the number of days passed from date to the current date
    public static int elapsedDays(int year, int month, int dayOfMonth) {
        Calendar c = Calendar.getInstance();
        long today = c.getTimeInMillis();
        c.set(year, month, dayOfMonth);
        long pickedDate = c.getTimeInMillis();
        long elapsedDays = (today - pickedDate) / 1000 / 60 / 60 / 24;
        Log.d(TAG, "elapsed days: " + elapsedDays);
        return (int) elapsedDays;
    }

    // return the number of days passed from date to the current date
    public static int elapsedDays(Calendar date) {
        Calendar c = Calendar.getInstance();
        long today = c.getTimeInMillis();
        c.set(Calendar.YEAR, date.get(Calendar.YEAR));
        c.set(Calendar.MONTH, date.get(Calendar.MONTH));
        c.set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH));
        long pickedDate = c.getTimeInMillis();
        long elapsedDays = (today - pickedDate) / 1000 / 60 / 60 / 24;
        Log.d(TAG, "elapsed days: " + elapsedDays);
        return (int) elapsedDays;
    }

}
