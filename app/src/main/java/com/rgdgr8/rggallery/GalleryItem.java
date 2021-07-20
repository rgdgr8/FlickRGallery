package com.rgdgr8.rggallery;

import android.graphics.Bitmap;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;

public class GalleryItem {
    private final String mCaption;
    private final String mId;
    private final String mUrl;
    private final String mOwner;
    private Bitmap mBitmap;

    public GalleryItem(String caption, String id, String url, String owner) {
        mCaption = caption;
        mId = id;
        mUrl = url;
        mOwner = owner;
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

    public String getOwner() {
        return mOwner;
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

    public Uri getPhotoPageUri() {
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }
}
