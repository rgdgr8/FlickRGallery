package com.rgdgr8.rggallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.io.IOException;

public class ThumbNailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbNailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;

    public ThumbNailDownloader() {
        super(TAG);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new MyHandler(this.getLooper());
    }

    public void queueMessage(T obj) {
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, obj).sendToTarget();
    }

    private class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_DOWNLOAD){
                GalleryItem item = (GalleryItem)msg.obj;
                String url = item.getUrl();

                try {
                    byte[] dataBytes = ImageFetcher.getUrlBytes(url);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(dataBytes,0,dataBytes.length);
                    item.getViewModel().setBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
