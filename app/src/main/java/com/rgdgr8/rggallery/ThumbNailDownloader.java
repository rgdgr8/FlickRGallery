package com.rgdgr8.rggallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import androidx.annotation.NonNull;

import java.io.IOException;

public class ThumbNailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbNailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int MESSAGE_PRE_DOWNLOAD = 1;
    private static final int MAX_PRE_DOWNLOAD = 20;
    public static boolean PRE_DOWNLOAD = true;
    private Handler mRequestHandler;
    private final Handler mResponseHandler;
    private final LruCache<String,Bitmap> lruCache;
    private GalleryFragment.ImageHolder imageHolder;
    private int lastPosition = 0;

    public ThumbNailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
        lruCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / (1024 * 5)));
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new MyHandler(this.getLooper());
    }

    public void queueMessage(int position, GalleryFragment.ImageHolder holder) {
        lastPosition = position;
        imageHolder = holder;
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget();
    }

    public void queueMessage(){
        int initialLastPosition = lastPosition;
        while (PRE_DOWNLOAD && (lastPosition-initialLastPosition)<=MAX_PRE_DOWNLOAD) {
            lastPosition++;
            if (ImageFetcher.getItemList().size() <= lastPosition) return;
            mRequestHandler.obtainMessage(MESSAGE_PRE_DOWNLOAD, ImageFetcher.getItemList().get(lastPosition)).sendToTarget();
        }
    }

    private class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            Log.d(TAG, "handleMessage: "+(msg.what==MAX_PRE_DOWNLOAD)+" "+msg.toString());
            if (msg.what == MESSAGE_DOWNLOAD){
                Log.d(TAG, "handleMessage: Download "+msg.toString());
                GalleryFragment.ImageHolder holder = (GalleryFragment.ImageHolder) msg.obj;
                GalleryItem item = holder.getBinding().getViewModel().getGalleryItem();
                Bitmap bitmap = lruCache.get(item.getId());
                if(bitmap!=null) {
                    item.setBitmap(bitmap);
                    setImage(bitmap,holder);
                    return;
                }

                String url = item.getUrl();
                try {
                    byte[] dataBytes = ImageFetcher.getUrlBytes(url);
                    bitmap = BitmapFactory.decodeByteArray(dataBytes,0,dataBytes.length);
                    lruCache.put(item.getId(),bitmap);
                    item.setBitmap(bitmap);
                    setImage(bitmap,holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if(msg.what == MESSAGE_PRE_DOWNLOAD){
                Log.d(TAG, "handleMessage: Pre-load "+msg.toString());
                GalleryItem item = (GalleryItem)msg.obj;
                Bitmap bitmap = lruCache.get(item.getId());
                if(bitmap!=null) {
                    return;
                }

                String url = item.getUrl();
                try {
                    byte[] dataBytes = ImageFetcher.getUrlBytes(url);
                    bitmap = BitmapFactory.decodeByteArray(dataBytes,0,dataBytes.length);
                    lruCache.put(item.getId(),bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setImage(Bitmap bitmap, GalleryFragment.ImageHolder holder){
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                holder.getBinding().getViewModel().notifyChange();
            }
        });
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
