package com.bignerdranch.android.photogallery;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.util.LruCache;

public class ImageCache extends LruCache<String, Bitmap> {
    public ImageCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(@NonNull String key, @NonNull Bitmap value) {
        return value.getByteCount() / 1024;
    }

    public Bitmap getBitmapFromMemory(String key){
        return this.get(key);
    }

    public void setBitmapToMemory(String key, Bitmap bitmap){
        if (getBitmapFromMemory(key) == null){
            this.put(key,bitmap);
        }
    }
}
