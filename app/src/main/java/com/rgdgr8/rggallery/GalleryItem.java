package com.rgdgr8.rggallery;

import android.graphics.Bitmap;

import org.jetbrains.annotations.NotNull;

public class GalleryItem {
    private final String mCaption;
    private final String mId;
    private final String mUrl;
    private GalleryItemViewModel mGVM;

    public GalleryItem(String caption, String id, String url){
        mCaption = caption;
        mId = id;
        mUrl = url;
    }

    public void setViewModel(GalleryItemViewModel GVM) {
        this.mGVM = GVM;
    }

    public GalleryItemViewModel getViewModel() {
        return mGVM;
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
