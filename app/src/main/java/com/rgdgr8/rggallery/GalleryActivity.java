package com.rgdgr8.rggallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.WebView;

import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final String INTENT_REFRESH = "refresh";
    private static final String SP_FIRST_LAUNCH = "firstLaunch";
    private static final String TAG = "GalleryActivity";
    private FragmentManager mGalleryFragmentManager;
    private Fragment mFragment;

    public static Intent newIntent(Context context) {
        Intent i = new Intent(context, GalleryActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(INTENT_REFRESH, true);
        return i;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: " + WebFragment.onWeb);
        if (WebFragment.onWeb) {
            WebView webView = null;
            if (mFragment instanceof WebFragment) {
                webView = ((WebFragment) mFragment).getWebView();
            } else if (mFragment == null) {
                mFragment = new GalleryFragment();
            }
            if (webView == null) {
                webView = ((GalleryFragment) mFragment).getWebFragment().getWebView();
            }

            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                mGalleryFragmentManager.beginTransaction().replace(R.id.frame, mFragment).commit();
            }
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Exit?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            GalleryActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton("No", null)
                    .create()
                    .show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_foreground);
        getSupportActionBar().setDisplayUseLogoEnabled(true);*/
        setContentView(R.layout.activity_gallery);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = preferences.getBoolean(SP_FIRST_LAUNCH, true);

        if (firstLaunch) {
            preferences.edit().putBoolean(SP_FIRST_LAUNCH, false).apply();

            Intent i = new Intent().setComponent(new ComponentName("com.miui.securitycenter"
                    , "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            List<ResolveInfo> activities = getPackageManager()
                    .queryIntentActivities(i, PackageManager.MATCH_DEFAULT_ONLY);

            if (activities.size() > 0) {
                new AlertDialog.Builder(this)
                        .setTitle("Needs auto-start permission for showing polling notifications")
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(i);
                            }
                        })
                        .create()
                        .show();
            }
        }

        boolean refresh = getIntent().getBooleanExtra(INTENT_REFRESH, false);
        if (refresh) {
            Log.d(TAG, "onCreate: refresh");
            ImageFetcher.PAGE_NO = 1;
            ImageFetcher.getItemList().clear();
            getIntent().putExtra(INTENT_REFRESH, false);
        }

        mGalleryFragmentManager = getSupportFragmentManager();
        mFragment = mGalleryFragmentManager.findFragmentById(R.id.frame);

        if (mFragment == null) {
            mFragment = new GalleryFragment();
        }
        if (!mFragment.isAdded()) {
            mGalleryFragmentManager.beginTransaction().add(R.id.frame, mFragment).commit();
        }
    }

    private final BroadcastReceiver onNotificationCreatedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // If we receive this, we're visible. So cancel the notification
            setResultCode(Activity.RESULT_CANCELED);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(PollService.ACTION_CHECK_NOTIFICATION);
        registerReceiver(onNotificationCreatedReceiver, filter, PollService.PERMISSION_NOTIFICATION_CREATED, null);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(onNotificationCreatedReceiver);
    }
}