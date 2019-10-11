package com.bignerdranch.android.draganddraw;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class BoxDrawingView extends View {
    private static final String TAG ="BoxDrawingView";
    private Box mCurrentBox;
    private ArrayList<Box> mBoxen = new ArrayList<>();

    private static final String LIST_BOX_KEY ="ListBoxKey";
    private static final String PARENT_STATUS_KEY ="ParentStatusKey";

    private Paint mBoxPaint;
    private Paint mBackgroundPaint;

    public BoxDrawingView(Context context) {
        super(context);
    }

    public BoxDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mBoxPaint = new Paint();
        mBoxPaint.setColor(0x22ff0000);

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PointF current = new PointF(event.getX(),event.getY());

        PointF touchPoint  = null;
        PointF touchPoint2 = null;
        for (int i=0;i<event.getPointerCount();i++) {
            if(event.getPointerId(i)==0)
                touchPoint = new PointF(event.getX(i), event.getY(i));
            if(event.getPointerId(i)==1)
                touchPoint2 = new PointF(event.getX(i), event.getY(i));
        }

        int actionMask = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerID = event.getPointerId(pointerIndex);

        String action ="";
        switch (actionMask){
            case MotionEvent.ACTION_DOWN:
                action = "ACTION_DOWN";
                mCurrentBox = new Box(touchPoint);
                mBoxen.add(mCurrentBox);
                break;
            case MotionEvent.ACTION_MOVE:
                action = "ACTION_MOVE";
                if (mCurrentBox != null) {
                    mCurrentBox.setCurrent(touchPoint);
                    if(touchPoint2 != null){
                        float angle = getAngle(mCurrentBox.getOrigin(),touchPoint2);
                        mCurrentBox.setAngle(angle);
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                action = "ACTION_UP";
                mCurrentBox = null;
                break;
            case MotionEvent.ACTION_CANCEL:
                action = "ACTION_CANCEL";
                mCurrentBox = null;
                break;
        }

        Log.i(TAG, action + " at x = " + current.x +
                ", y = " + current.y + "pointer ID = " + pointerID);

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Заполнение фона
        canvas.drawPaint(mBackgroundPaint);

        for (Box box: mBoxen){
            float left = Math.min(box.getOrigin().x, box.getCurrent().x);
            float right = Math.max(box.getOrigin().x, box.getCurrent().x);
            float top = Math.min(box.getOrigin().y, box.getCurrent().y);
            float bottom = Math.max(box.getOrigin().y,box.getCurrent().y);

            float centerX = (right + left)/2;
            float centerY = (bottom + top)/2;
            float angle = box.getAngle();

            canvas.save();
            canvas.rotate(angle,centerX,centerY);
            canvas.drawRect(left,top,right,bottom,mBoxPaint);
            canvas.restore();
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle statusSave = new Bundle();
        statusSave.putParcelableArrayList(LIST_BOX_KEY,mBoxen);
        statusSave.putParcelable(PARENT_STATUS_KEY,
                super.onSaveInstanceState());

        return statusSave;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state != null && state instanceof Bundle) {
            Bundle parentStatus =(Bundle) state;
            mBoxen = parentStatus.getParcelableArrayList(LIST_BOX_KEY);
            super.onRestoreInstanceState(parentStatus
                    .getParcelable(PARENT_STATUS_KEY));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    protected float getAngle(PointF origin, PointF current){
        float angle =(float) Math.toDegrees(
                Math.atan2(current.y - origin.y,current.x - origin.x));
        return angle;
    }
}
