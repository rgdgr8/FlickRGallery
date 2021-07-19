package com.rgdgr8.rggallery;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;

public class NotificationReceiver extends BroadcastReceiver {
    private static final int NOTIFY_ID = 1;
    public static final String NOTIFICATION_CHANNEL_ID = "NEW_PHOTOS_FOUND";
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if(getResultCode()== Activity.RESULT_CANCELED){
            Log.i(TAG, "onReceive: notification cancelled");
            return;
        }

        Notification notification = (Notification)intent
                .getParcelableExtra(PollService.INTENT_CHECK_NOTIFICATION);

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(context);
        createNotificationChannel(managerCompat);
        managerCompat.notify(NOTIFY_ID, notification);
    }

    private void createNotificationChannel(NotificationManagerCompat mNotificationManager) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "New Photos Notification Channel",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
    }
}
