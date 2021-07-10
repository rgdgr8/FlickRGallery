package com.rgdgr8.rggallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import java.lang.ref.WeakReference;

public class GalleryItemViewModel extends BaseObservable {
    private GalleryItem mGalleryItem;
    private Bitmap mBitmap;
    private static WeakReference<Context> mCtx;

    public GalleryItemViewModel(Context context){
        if (mCtx==null) {
            mCtx = new WeakReference<>(context);
        }
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
        notifyChange();
    }

    public void setGalleryItem(GalleryItem mGalleryItem) {
        this.mGalleryItem = mGalleryItem;
    }

    @Bindable
    public Drawable getImage(){
        if(mBitmap==null){
            return ContextCompat.getDrawable(mCtx.get(), R.drawable.ic_launcher_foreground);
        }
        return new BitmapDrawable(mCtx.get().getResources(),mBitmap);
    }

    public String getTitle(){return mGalleryItem.getCaption();}
}
