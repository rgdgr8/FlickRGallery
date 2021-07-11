package com.rgdgr8.rggallery;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

public class GalleryItem {
    private final String mCaption;
    private final String mId;
    private final String mUrl;
    private Bitmap mBitmap;

    public GalleryItem(String caption, String id, String url){
        mCaption = caption;
        mId = id;
        mUrl = url;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public String getCaption() {
        return mCaption;
    }

    public String getId() {
        return mId;
    }

    public String getUrl() {
        return mUrl;
    }

    @Override
    public @NotNull String toString() {
        return mCaption;
    }
}
