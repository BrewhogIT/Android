package com.bignerdranch.android.sunset;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;

public class SunsetFragment extends Fragment {
    private View mSceneView;
    private View mSkyView;
    private View mSunView;
    private View mSunReflection;
    private View mSea;

    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;
    private int mCurrentSkyColor;
    private int mCurrentNightSkyColor;

    private boolean isSunset;
    private AnimatorSet sunriseAnimatorSet;
    private AnimatorSet sunsetAnimatorSet;

    private float sunYStart;
    private float sunX;
    private float reflectionX;
    private float reflectionYStart;

    private static final int DURATION = 3000;

    public static SunsetFragment newInstance(){
        return new SunsetFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset,container,false);

        mSceneView = view;
        mSkyView = view.findViewById(R.id.sky);
        mSunView = view.findViewById(R.id.sun);
        mSunReflection = view.findViewById(R.id.sunReflection);
        mSea = view.findViewById(R.id.sea);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);
        mCurrentSkyColor = mBlueSkyColor;
        mCurrentNightSkyColor = mSunsetSkyColor;


        isSunset = false;

        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSunset){
                    startSunriseAnimation();
                    if (sunsetAnimatorSet != null){
                        sunsetAnimatorSet.end();
                        sunsetAnimatorSet = null;
                    }
                }else{
                    startSunsetAnimation();
                    if (sunriseAnimatorSet != null){
                        sunriseAnimatorSet.end();
                        sunriseAnimatorSet = null;
                    }
                }
                startSunShudding();
                isSunset = !isSunset;
            }
        });

        ViewTreeObserver observer = view.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
//                ViewTreeObserver observer = mSceneView.getViewTreeObserver();
//                observer.removeOnGlobalLayoutListener(this);

                sunYStart = mSunView.getTop();
                reflectionYStart = mSunReflection.getTop();
            }
        });

        return view;
    }

    private void startSunsetAnimation(){
        long duration = (long) (DURATION / (mSkyView.getHeight() - mSunView.getTop())
                * (mSkyView.getHeight() - sunYStart));

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunView,"y",sunYStart,mSkyView.getHeight())
                .setDuration(duration);
        heightAnimator.setInterpolator(new AccelerateInterpolator());
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sunYStart =(float) animation.getAnimatedValue();
            }
        });

        ObjectAnimator reflectionHeightAnimator = ObjectAnimator
                .ofFloat(mSunReflection,"y",reflectionYStart, - mSunReflection.getHeight())
                .setDuration(duration);
        reflectionHeightAnimator.setInterpolator(new AccelerateInterpolator());
        reflectionHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                reflectionYStart = (float)animation.getAnimatedValue();
            }
        });

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView,"backgroundColor",mCurrentSkyColor,mSunsetSkyColor)
                .setDuration(duration);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
        sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentSkyColor = (int)animation.getAnimatedValue();
            }
        });

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView,"backgroundColor",mCurrentNightSkyColor,mNightSkyColor)
                .setDuration(DURATION);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());
        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentNightSkyColor = (int)animation.getAnimatedValue();
            }
        });

        sunsetAnimatorSet = new AnimatorSet();
        sunsetAnimatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimator)
                .with(reflectionHeightAnimator)
                .before(nightSkyAnimator);
        sunsetAnimatorSet.start();
    }
    private void startSunriseAnimation(){
        long duration = (long) (DURATION / (mSkyView.getHeight() - mSunView.getTop()) *
                (sunYStart- mSunView.getTop()));
        long nightDuration = (long)(DURATION*((double)(mSunsetSkyColor - mCurrentNightSkyColor) /
                (double)(mSunsetSkyColor-mNightSkyColor)));

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunView,"y",sunYStart,mSunView.getTop())
                .setDuration(duration);
        heightAnimator.setInterpolator(new AccelerateInterpolator());
        heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                sunYStart = (float) animation.getAnimatedValue();
            }
        });

        ObjectAnimator reflectionHeightAnimator = ObjectAnimator
                .ofFloat(mSunReflection,"y",reflectionYStart,mSunReflection.getTop())
                .setDuration(duration);
        reflectionHeightAnimator.setInterpolator(new AccelerateInterpolator());
        reflectionHeightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                reflectionYStart = (float) animation.getAnimatedValue();
            }
        });

        ObjectAnimator sunsetSkyAnimator = ObjectAnimator
                .ofInt(mSkyView,"backgroundColor",mCurrentSkyColor,mBlueSkyColor)
                .setDuration(duration);
        sunsetSkyAnimator.setEvaluator(new ArgbEvaluator());
        sunsetSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentSkyColor = (int)animation.getAnimatedValue();
            }
        });

        ObjectAnimator nightSkyAnimator = ObjectAnimator
                .ofInt(mSkyView,"backgroundColor",mCurrentNightSkyColor,mSunsetSkyColor)
                .setDuration(nightDuration);
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());
        nightSkyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCurrentNightSkyColor = (int)animation.getAnimatedValue();
            }
        });

        sunriseAnimatorSet = new AnimatorSet();
        sunriseAnimatorSet
                .play(heightAnimator)
                .with(sunsetSkyAnimator)
                .with(reflectionHeightAnimator)
                .after(nightSkyAnimator);
        sunriseAnimatorSet.start();
    }
    private void startSunShudding(){
        sunX = mSunView.getLeft();
        reflectionX = mSunReflection.getLeft();

        ObjectAnimator sunShudderAnimation = ObjectAnimator
                .ofFloat(mSunView,"x",sunX,sunX+5)
                .setDuration(100);
        sunShudderAnimation.setRepeatCount(30);

        ObjectAnimator sunReflectionShudderAnimation = ObjectAnimator
                .ofFloat(mSunReflection,"x",reflectionX,reflectionX+5)
                .setDuration(100);
        sunReflectionShudderAnimation.setRepeatCount(30);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet
                .play(sunShudderAnimation)
                .with(sunReflectionShudderAnimation);
        animatorSet.start();
    }
}
