package com.github.cris16228.libcore;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LongUtils {

    Context context;

    public static LongUtils with(Context _context) {
        LongUtils longUtils = new LongUtils();
        longUtils.context = _context;
        return longUtils;
    }

    public static String toReadableTimeFull(long timeInMillis, String timeFormat, boolean hideUnnecessary) {
        char[] units = {'y', 'M', 'w', 'd', 'h', 'm', 's'};
        long[] divisor = {
                365 * 24 * 60 * 60 * 1000L,
                30 * 24 * 60 * 60 * 1000L,
                7 * 24 * 60 * 60 * 1000L,
                24 * 60 * 60 * 1000L,
                60 * 60 * 1000L,
                60 * 1000L,
                1000L
        };
        String[] unitNames = {" years", " months", " weeks", " days", " hours", " minutes", " seconds"};
        boolean past = timeInMillis < 0;
        if (past) timeInMillis *= -1;
        StringBuilder timeBuf = new StringBuilder();

        for (int i = 0; i < timeFormat.length(); i++) {
            char currentUnit = timeFormat.charAt(i);
            int unitIndex = -1;

            for (int j = 0; j < units.length; j++) {
                if (units[j] == currentUnit) {
                    unitIndex = j;
                    break;
                }
            }

            if (unitIndex != -1) {
                long time = timeInMillis / divisor[unitIndex];
                if (time > 0L) {
                    timeInMillis -= time * divisor[unitIndex];
                    prependTimeAndUnit(timeBuf, time, time > 1L ? unitNames[unitIndex] : unitNames[unitIndex].substring(0, unitNames[unitIndex].length() - 1));
                    if (hideUnnecessary)
                        break;
                }
            }
        }
        if (past) {
            timeBuf.append("ago");
        }
        return timeBuf.toString().trim();
    }

    private static void prependTimeAndUnit(StringBuilder timeBuf, long time, String unit) {
        timeBuf.append(time).append(unit).append(" ");
    }

    private void prependTimeAndUnit(StringBuffer timeBuf, long time, String unit) {
        if (time < 1) {
            return;
        }

        if (timeBuf.length() > 0) {
            timeBuf.insert(0, " ");
        }

        timeBuf.insert(0, unit);
        timeBuf.insert(0, time);
    }

    public static String getSize(long size) {
        long n = 1000;
        String s;
        double kb = size / (double) n;
        double mb = kb / n;
        double gb = mb / n;
        double tb = gb / n;

        if (size < n) {
            s = size + " Bytes";
        } else if (size < n * n) {
            s = String.format(Locale.US, "%.2f KB", kb);
        } else if (size < n * n * n) {
            s = String.format(Locale.US, "%.2f MB", mb);
        } else if (size < n * n * n * n) {
            s = String.format(Locale.US, "%.2f GB", gb);
        } else {
            s = String.format(Locale.US, "%.2f TB", tb);
        }

        return s;
    }

    public String longToDate(long date, String pattern) {
        if (StringUtils.isEmpty(pattern))
            pattern = "EEEE, dd MMMM yyyy HH:mm";
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(date);
    }

    public String toReadableTime(long timeInMillis) {

        if (timeInMillis < 1) {
            return String.valueOf(timeInMillis);
        }

        StringBuffer timeBuf = new StringBuffer();

        // second (1000ms) & above
        long time = timeInMillis / 1000;
        if (time < 1) {
            return "now";
        }

        long seconds = time % 60;
        prependTimeAndUnit(timeBuf, seconds, "s");

        // minute(60s) & above
        time = time / 60;
        if (time < 1) {
            return timeBuf.toString();
        }

        long minutes = time % 60;
        prependTimeAndUnit(timeBuf, minutes, "m");

        // hour(60m) & above
        time = time / 60;
        if (time < 1) {
            return timeBuf.toString();
        }

        long hours = time % 24;
        prependTimeAndUnit(timeBuf, hours, "h");

        // day(24h) & above
        time = time / 24;
        if (time < 1) {
            return timeBuf.toString();
        }

        long day = time % 365;
        prependTimeAndUnit(timeBuf, day, "d");

        // year(365d) ...
        time = time / 365;
        if (time < 1) {
            return timeBuf.toString();
        }

        prependTimeAndUnit(timeBuf, time, "y");

        return timeBuf.toString();
    }

    public String toReadableTimeFull(long timeInMillis) {
        return toReadableTimeFull(timeInMillis, "dhm", false);
    }

    public static String convertDuration(long millis) {
        if (millis <= 0) {
            return "00:00";
        }
        if (TimeUnit.MILLISECONDS.toHours(millis) > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d",
                    TimeUnit.MILLISECONDS.toHours(millis),
                    TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                    TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
        }
        return String.format(Locale.getDefault(), "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(millis) % 60,
                TimeUnit.MILLISECONDS.toSeconds(millis) % 60);
    }
}
