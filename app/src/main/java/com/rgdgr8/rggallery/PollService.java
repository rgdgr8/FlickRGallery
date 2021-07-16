package com.rgdgr8.rggallery;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.TimeUnit;

public class PollService extends IntentService {
    public static final String SP_FIRST_ID_KEY = "lastId";
    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(10);
    private static final int PI_REQUEST_CODE = 0;
    private static final int NOTIFY_ID = 1;
    private static final String NOTIFICATION_CHANNEL_ID = "NEW_PHOTOS_FOUND";

    public static Intent pollServiceIntent(Context context) {
        return new Intent(context, PollService.class);
    }

    public static boolean isAlarmOn(Context context) {
        Intent i = pollServiceIntent(context);
        PendingIntent pi = PendingIntent.getService(context, PI_REQUEST_CODE, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PollService() {
        super(TAG);
    }

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent i = pollServiceIntent(context);
        PendingIntent pi = PendingIntent.getService(context, PI_REQUEST_CODE, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (isOn) {
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndconnected()) {
            Log.d(TAG, "onHandleIntent: No network");
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String firstId = sharedPreferences.getString(SP_FIRST_ID_KEY, null);

        try {
            if (GalleryFragment.mSearchViewQuery.equals("")) {
                ImageFetcher.getRecentImages(true);
            } else {
                ImageFetcher.getSearchedImages(GalleryFragment.mSearchViewQuery, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newFirstId = ImageFetcher.getRecentItemsList().get(0).getId();

        Log.d(TAG, "onHandleIntent: newFirstId = " + newFirstId);

        if (newFirstId.equals(firstId)) {
            Log.i(TAG, "Old Result");
        } else {
            Log.i(TAG, "New Result");

            Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("New Photos available in " + getResources().getString(R.string.app_name))
                    .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                    .setContentIntent(PendingIntent.getActivity(this, PI_REQUEST_CODE
                            , GalleryActivity.newIntent(this), 0))
                    .setAutoCancel(true)
                    .build();

            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
            createNotificationChannel(managerCompat);
            managerCompat.notify(NOTIFY_ID, notification);
        }

        sharedPreferences.edit().putString(SP_FIRST_ID_KEY, newFirstId).apply();
    }

    private static void createNotificationChannel(NotificationManagerCompat mNotificationManager) {
        NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "New Photos Notification Channel",
                NotificationManager.IMPORTANCE_HIGH);
        mNotificationManager.createNotificationChannel(channel);
    }

    private boolean isNetworkAvailableAndconnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = (manager.getActiveNetworkInfo() != null);
        boolean isNetworkConnected = (isNetworkAvailable && manager.getActiveNetworkInfo().isConnected());

        return isNetworkConnected;
    }
}
