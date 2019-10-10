package com.bignerdranch.android.draganddraw;

import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Box implements Parcelable {
    private static final String TAG = "Box";
    private PointF mOrigin;
    private PointF mCurrent;
    private float angle;

    public Box(PointF origin) {
        mOrigin = origin;
        mCurrent = origin;
        angle = 0;
    }

    public Box(Parcel in) {
        mOrigin.readFromParcel(in);
        mCurrent.readFromParcel(in);
    }

    public PointF getCurrent() {
        return mCurrent;
    }
    public void setCurrent(PointF current) {
        mCurrent = current;
    }
    public PointF getOrigin() {
        return mOrigin;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public static final Parcelable.Creator<Box> CREATOR = new Parcelable.Creator<Box>() {
        @Override
        public Box createFromParcel(Parcel in) {
            return new Box(in);
        }

        @Override
        public Box[] newArray(int size) {
            return new Box[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        mOrigin.writeToParcel(dest,flags);
        mCurrent.writeToParcel(dest,flags);
    }
}
