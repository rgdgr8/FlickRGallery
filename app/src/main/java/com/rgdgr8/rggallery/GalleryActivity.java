package com.rgdgr8.rggallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class GalleryActivity extends AppCompatActivity {
    private static final String INTENT_REFRESH = "refresh";

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
        setContentView(R.layout.activity_gallery);

        boolean refresh = getIntent().getBooleanExtra(INTENT_REFRESH,false);
        if(refresh){
            ImageFetcher.PAGE_NO = 1;
        }

        FragmentManager fm = getSupportFragmentManager();
        GalleryFragment gf = (GalleryFragment) fm.findFragmentById(R.id.frame);

        if (gf==null){
            gf = new GalleryFragment();
            fm.beginTransaction().add(R.id.frame,gf).commit();
        }
    }
}