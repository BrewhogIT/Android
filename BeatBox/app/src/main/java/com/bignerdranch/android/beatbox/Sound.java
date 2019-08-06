package com.bignerdranch.android.beatbox;

public class Sound {
    private String mAssetPath;
    private String mName;
    private Integer mSoundId;
    private float mSoundSpeed;

    public Sound(String assetPath) {
        mAssetPath = assetPath;
        String[]components = assetPath.split("/");
        String fileName = components[components.length-1];
        mName = fileName.replace(".wav","");
        mSoundSpeed =1.0f;
    }

    public String getAssetPath() {
        return mAssetPath;
    }

    public String getName() {
        return mName;
    }

    public Integer getSoundId() {
        return mSoundId;
    }

    public void setSoundId(Integer soundId) {
        mSoundId = soundId;
    }

    public float getSoundSpeed() {
        return mSoundSpeed;
    }

    public void setSoundSpeed(float soundSpeed) {
        mSoundSpeed = soundSpeed;
    }
}
