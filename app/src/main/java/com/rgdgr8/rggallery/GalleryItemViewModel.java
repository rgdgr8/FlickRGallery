package com.rgdgr8.rggallery;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class GalleryItemViewModel extends BaseObservable {
    GalleryItem mGalleryItem;

    public void setGalleryItem(GalleryItem mGalleryItem) {
        this.mGalleryItem = mGalleryItem;
        notifyChange();
    }

    @Bindable
    public String getTitle(){return mGalleryItem.getCaption();}
}
