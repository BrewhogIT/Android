package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class ImageActivity extends AppCompatActivity {

    public static Intent newInstance(Context context, Uri photoUri){
        Intent intent = new Intent(context, ImageActivity.class);
        intent.setData(photoUri);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        ImageView view = findViewById(R.id.enlarged_image);
        view.setImageURI(getIntent().getData());
    }

    public static void startWithTransition(Activity activity, Intent intent, View sourceView){
        ViewCompat.setTransitionName(sourceView,"image");
        ActivityOptionsCompat options = ActivityOptionsCompat
                .makeSceneTransitionAnimation(activity,sourceView,"image");

        activity.startActivity(intent,options.toBundle());
    }
}
