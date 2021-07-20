package com.rgdgr8.rggallery;

import android.app.Activity;
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
    public static final String PERMISSION_NOTIFICATION_CREATED = "com.rgdgr8.rggallery.NOTIFICATION_CREATED";
    public static final String ACTION_CHECK_NOTIFICATION = "com.rgdgr8.rggallery.CHECK_NOTIFICATION";
    public static final String INTENT_CHECK_NOTIFICATION = "notification_check";

    private static final String SP_FIRST_ID_KEY = "lastId";
    public static final String SP_SEARCH = "search";
    public static final String SP_SERVICE_STATE_KEY = "service_state";
    private static final String TAG = "PollService";
    private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);
    private static final int PI_REQUEST_CODE = 0;

    private static Intent pollServiceIntent(Context context) {
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
        Log.d(TAG, "setServiceAlarm: ");
        if (isAlarmOn(context) == isOn) return;

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

        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(SP_SERVICE_STATE_KEY, isOn).apply();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()) {
            Log.i(TAG, "onHandleIntent: No network");
            return;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String firstId = sharedPreferences.getString(SP_FIRST_ID_KEY, null);
        String photoType = GalleryFragment.mSearchViewQuery;
        if (photoType == null) {
            photoType = sharedPreferences.getString(SP_SEARCH, null);
        }
        Log.d(TAG, "onHandleIntent: photoType = " + photoType + ", SP_SEARCH_KEY = " + SP_SEARCH);

        try {
            if (photoType == null) {
                photoType = "Feed";
                ImageFetcher.getRecentImages(true);
            } else {
                sharedPreferences.edit().putString(SP_SEARCH, photoType).apply();
                ImageFetcher.getSearchedImages(photoType, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        String newFirstId = "";
        if (ImageFetcher.getCheckItemsList().size() > 0) {
            newFirstId = ImageFetcher.getCheckItemsList().get(0).getId();
            ImageFetcher.getCheckItemsList().clear();
        }
        Log.d(TAG, "onHandleIntent: newFirstId = " + newFirstId);

        if (newFirstId.equals(firstId)) {
            Log.i(TAG, "Old Result");
        } else if (firstId != null) {
            Log.i(TAG, "New Result");

            Notification notification = new Notification.Builder(this, NotificationReceiver.NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("New " + photoType + " Photos available in " + getResources().getString(R.string.app_name))
                    .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                    .setContentIntent(PendingIntent.getActivity(this, PI_REQUEST_CODE
                            , GalleryActivity.newIntent(this), 0))
                    .setAutoCancel(true)
                    .build();

            showNotification(notification);
        }

        sharedPreferences.edit().putString(SP_FIRST_ID_KEY, newFirstId).apply();
    }

    private void showNotification(Notification notification) {
        Intent i = new Intent(ACTION_CHECK_NOTIFICATION);
        i.putExtra(INTENT_CHECK_NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERMISSION_NOTIFICATION_CREATED, null, null, Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = (manager.getActiveNetworkInfo() != null);
        boolean isNetworkConnected = (isNetworkAvailable && manager.getActiveNetworkInfo().isConnected());

        return isNetworkConnected;
    }
}
