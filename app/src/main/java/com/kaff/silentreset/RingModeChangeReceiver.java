package com.kaff.silentreset;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

public class RingModeChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        unsetNotification(context);
        if (!isAutoResetEnabled(context)) return;
        if (isRingmodeNormal(context)) return;
        setAlarm(context);
    }

    private boolean isRingmodeNormal(Context context) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int ringerMode = audioManager.getRingerMode();
        return (ringerMode == AudioManager.RINGER_MODE_NORMAL);
    }

    private boolean isAutoResetEnabled(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        return sp.getBoolean("enable_auto_reset", false);
    }

    private int getResetTimer(Context  context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String s = sp.getString("reset_after_minutes", "30");
        if ("0".equals(s)) {
            //custom setting
            s = sp.getString("reset_after_minutes_custom", "30");
        }

        int minute = 30;
        try {
            minute = Integer.parseInt(s);
            if (minute <= 0) minute = 1;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            return minute;
        }
    }

    private void setNotification(Context context) {
        //通知栏
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        Notification notification = new NotificationCompat.Builder(context)
                .setContentTitle("定时退出静音")
                .setContentText("将在设定的时间后退出静音模式")
                .setWhen(System.currentTimeMillis() + 2000)
                .setSmallIcon(R.drawable.ic_ringup_small)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_ringup))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notification);

        //一个标志，使得在已有pendingIntent的情况下，程序启动时不会重新设定alarm
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pending_reset", true);
        editor.apply();
    }

    private void unsetNotification(Context context) {
        //取消通知
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(1);

        //取消标志
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("pending_reset", false);
        editor.apply();
    }

    private void setAlarm(Context context) {
        Intent i = new Intent("com.kaff.RESET_RINGER");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pi);

        int m = getResetTimer(context);
        Toast.makeText(context, "自动退出静音：" + m + " 分钟后", Toast.LENGTH_SHORT).show();
        setNotification(context);
        long triggerTime = SystemClock.elapsedRealtime() + m * 60 * 1000;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pi);
    }
}
