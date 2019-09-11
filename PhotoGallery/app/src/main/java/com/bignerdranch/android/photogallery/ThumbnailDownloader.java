package com.bignerdranch.android.photogallery;

import android.app.ActivityManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.support.v4.content.ContextCompat.getSystemService;

public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;

    private boolean mHasQuit = false;
    private Handler mRequestHandler;
    private ConcurrentMap<T,String> mRequestMap = new ConcurrentHashMap<>();
    private Handler mResponseHandler;
    private ThumbnailDownloadListener<T> mThumbnailDownloadListener;
    private ImageCache mImageCache;

    public interface ThumbnailDownloadListener<T>{
        void onThumbnailDownloaded(T target,Bitmap thumbnail);
    }

    public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
        mThumbnailDownloadListener = listener;
    }

    public ThumbnailDownloader(Handler responseHandler,ActivityManager am) {
        super(TAG);
        mResponseHandler = responseHandler;

        int memClass = am.getMemoryClass();
        int cacheSize = 1024 * 1024 * memClass / 8;
        mImageCache = new ImageCache(cacheSize);
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD){
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: "
                            + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbnail(T target, String url){
        Log.i(TAG,"Got a URL: " + url);

        if (url == null){
            mRequestMap.remove(target);
        }else{
            mRequestMap.put(target,url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD,target)
                    .sendToTarget();
        }
    }

    public void clearQueue(){
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target){
        try{
            final String url = mRequestMap.get(target);
            final Bitmap bitmap;
            if (url == null){
                return;
            }

            if (mImageCache.getBitmapFromMemory(url) == null){
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory
                        .decodeByteArray(bitmapBytes,0,bitmapBytes.length);
                mImageCache.setBitmapToMemory(url,bitmap);
                Log.i(TAG,"Bitmap created");
            }else{
                bitmap = mImageCache.getBitmapFromMemory(url);
            }


            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mRequestMap.get(target) != url || mHasQuit){
                        return;
                    }

                    mRequestMap.remove(target);
                    mThumbnailDownloadListener.onThumbnailDownloaded(target,bitmap);
                }
            });
        }catch (IOException ioe){
            Log.e(TAG,"Error downloading image", ioe);
        }
    }
}
