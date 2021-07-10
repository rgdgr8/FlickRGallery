package com.rgdgr8.rggallery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

public class GalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        FragmentManager fm = getSupportFragmentManager();
        GalleryFragment gf = (GalleryFragment) fm.findFragmentById(R.id.frame);

        if (gf==null){
            gf = new GalleryFragment();
            fm.beginTransaction().add(R.id.frame,gf).commit();
        }
    }
}