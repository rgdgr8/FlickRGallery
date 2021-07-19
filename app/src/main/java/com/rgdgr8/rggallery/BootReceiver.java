package com.rgdgr8.rggallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "RgBootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isOn = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PollService.SP_SERVICE_STATE_KEY,false);
        Log.d(TAG, "onReceive: action = "+intent.getAction()+", isOn = "+isOn);

        PollService.setServiceAlarm(context,isOn);
        Intent i = GalleryActivity.newIntent(context);
        context.startActivity(i);
    }
}
