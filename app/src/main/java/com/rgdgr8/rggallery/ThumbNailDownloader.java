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
    private static final int POST_FOR_MESSAGE_PRE_DOWNLOAD = 2;
    private static final int MAX_PRE_DOWNLOAD = 15;
    private Handler mRequestHandler;
    private final Handler mResponseHandler;
    private final LruCache<String,Bitmap> lruCache;
    private final HandlerThread preLoadThread;
    private final MyHandler preLoadHandler;

    public ThumbNailDownloader(Handler handler) {
        super(TAG);
        mResponseHandler = handler;
        lruCache = new LruCache<>((int) (Runtime.getRuntime().maxMemory() / (1024 * 6)));
        preLoadThread = new HandlerThread("Cache");
        preLoadThread.start();
        Looper looper = preLoadThread.getLooper();
        preLoadHandler = new MyHandler(looper);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new MyHandler(this.getLooper());
    }

    public void queueMessage(int position, GalleryFragment.ImageHolder holder) {
        if(position%MAX_PRE_DOWNLOAD==0)
            queueMessagesForPreLoad(position);
        mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, holder).sendToTarget();
    }

    private void queueMessagesForPreLoad(int position){
        preLoadHandler.obtainMessage(POST_FOR_MESSAGE_PRE_DOWNLOAD, position,-1).sendToTarget();
    }

    private class MyHandler extends Handler{
        public MyHandler(Looper looper){
            super(looper);
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            if (msg.what == MESSAGE_DOWNLOAD){
                Log.d(TAG, "handleMessage: Download "+msg.toString());
                GalleryFragment.ImageHolder holder = (GalleryFragment.ImageHolder) msg.obj;
                GalleryItem item = holder.getBinding().getViewModel().getGalleryItem();
                Bitmap bitmap = lruCache.get(item.getId());
                if(bitmap!=null) {
                    Log.d(TAG, "handleMessage: cache hit");
                    item.setBitmap(bitmap);
                    setImage(bitmap,holder);
                    return;
                }
                Log.d(TAG, "handleMessage: cache miss");
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
            else if(msg.what == POST_FOR_MESSAGE_PRE_DOWNLOAD){
                Log.d(TAG, "handleMessage: Post for Pre-load");
                int position = msg.arg1;
                for (int i = position+MAX_PRE_DOWNLOAD; i < position+(5*MAX_PRE_DOWNLOAD); i++) {
                    if (ImageFetcher.getItemList().size() <= i) return;
                    this.obtainMessage(MESSAGE_PRE_DOWNLOAD,ImageFetcher.getItemList().get(i)).sendToTarget();
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

    @Override
    public boolean quit() {
        preLoadThread.quit();
        return super.quit();
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }
}
