package com.rgdgr8.rggallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final String INTENT_REFRESH = "refresh";
    private static final String SP_FIRST_LAUNCH = "firstLaunch";
    private static final String TAG = "GalleryActivity";

    public static Intent newIntent(Context context){
        Intent i = new Intent(context,GalleryActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(INTENT_REFRESH,true);
        return i;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GalleryActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No",null)
                .create()
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_foreground);
        getSupportActionBar().setDisplayUseLogoEnabled(true);*/
        setContentView(R.layout.activity_gallery);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstLaunch = preferences.getBoolean(SP_FIRST_LAUNCH,true);

        if(firstLaunch){
            preferences.edit().putBoolean(SP_FIRST_LAUNCH, false).apply();

            Intent i = new Intent().setComponent(new ComponentName("com.miui.securitycenter"
                    ,"com.miui.permcenter.autostart.AutoStartManagementActivity"));
            List<ResolveInfo> activities = getPackageManager()
                    .queryIntentActivities(i,PackageManager.MATCH_DEFAULT_ONLY);

            if(activities.size()>0){
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

        boolean refresh = getIntent().getBooleanExtra(INTENT_REFRESH,false);
        if(refresh){
            Log.d(TAG, "onCreate: refresh");
            ImageFetcher.PAGE_NO = 1;
            ImageFetcher.getItemList().clear();
            getIntent().putExtra(INTENT_REFRESH,false);
        }

        FragmentManager fm = getSupportFragmentManager();
        GalleryFragment gf = (GalleryFragment) fm.findFragmentById(R.id.frame);

        if (gf==null){
            gf = new GalleryFragment();
            fm.beginTransaction().add(R.id.frame,gf).commit();
        }
    }
}