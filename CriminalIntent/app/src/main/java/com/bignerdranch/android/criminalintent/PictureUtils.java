package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.media.ExifInterface;
import android.widget.Switch;

import java.io.IOException;

public class PictureUtils {
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);

        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        int inSampleSize = 1;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            float heightScale = srcHeight / destHeight;
            float widthScale = srcWidth / destWidth;

            inSampleSize = Math.round(heightScale > widthScale ? heightScale : widthScale);
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        int angle = getPictureDegree(path);
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        bitmap = rotateBitmap(angle,bitmap);

        return bitmap;
    }

    private static int getPictureDegree(String path) {
        int degree = 0;
        int orientation = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);

            orientation = exifInterface.
                    getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
            switch (orientation){
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

    private static Bitmap rotateBitmap(int angle, Bitmap bitmap){
        Bitmap newBitmap = null;

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        try{
            newBitmap = Bitmap.createBitmap(bitmap,
                    0,
                    0,
                    bitmap.getWidth(),
                    bitmap.getHeight(),
                    matrix,
                    true);
        }catch(OutOfMemoryError e){
        }

        if (newBitmap == null){
            newBitmap = bitmap;
        }

        if (newBitmap != bitmap){
            bitmap.recycle();
        }
        return newBitmap;
    }


}
