package com.rgdgr8.rggallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.lang.ref.WeakReference;

public class GalleryItemViewModel extends BaseObservable {
    private static final String TAG = "GModelView";
    private GalleryItem mGalleryItem;
    private static WeakReference<Context> mCtx;

    public GalleryItemViewModel(Context context){
        if (mCtx==null) {
            mCtx = new WeakReference<>(context);
        }
    }

    public void setGalleryItem(GalleryItem mGalleryItem) {
        this.mGalleryItem = mGalleryItem;
        notifyChange();
    }

    public GalleryItem getGalleryItem() {
        return mGalleryItem;
    }

    @Bindable
    public Drawable getImage(){
        if(mGalleryItem.getBitmap()==null){
            Log.d(TAG, "getImage: default");
            return ContextCompat.getDrawable(mCtx.get(), R.drawable.ic_launcher_foreground);
        }
        Log.d(TAG, "getImage: fetched");
        return new BitmapDrawable(mCtx.get().getResources(),mGalleryItem.getBitmap());
    }

    @Bindable
    public String getTitle(){
        if(mGalleryItem!=null)
        return mGalleryItem.getCaption();

        return "";
    }
}
