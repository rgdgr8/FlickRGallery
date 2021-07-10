package com.rgdgr8.rggallery;

import org.jetbrains.annotations.NotNull;

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;

    public GalleryItem(String caption, String id, String url){
        mCaption = caption;
        mId = id;
        mUrl = url;
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
