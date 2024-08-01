package com.github.cris16228.libcore.alarmmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;


public class AlarmManagerUtils {

    public Intent intent;
    Context context;
    /*public static AlarmManagerUtils instance;*/
    AlarmManager alarmManager;

    public AlarmManagerUtils(Context _context) {
        context = _context;
        alarmManager = (AlarmManager) _context.getSystemService(Context.ALARM_SERVICE);
    }

    public AlarmManager getAlarmManager() {
        return alarmManager;
    }

    public void createIntent(Class<? extends BroadcastReceiver> broadcastReceiver) {
        intent = new Intent(context, broadcastReceiver);
    }

    public Intent getIntent() {
        return intent;
    }

    public void createRepeating(Calendar mCalendar, int requestCode, long intervalMillis) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, mCalendar.getTimeInMillis(), intervalMillis, pendingIntent);
    }

    public void cancelAlarm(int requestCode) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }
}
