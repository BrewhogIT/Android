package com.bignerdranch.android.criminalintent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.zip.Inflater;

public class PictureFragment extends DialogFragment {
    private static final String PHOTO_KEY = "Photo_file";
    private ImageView image;

    public static PictureFragment newInstance(File photoFile){
        Bundle args = new Bundle();
        args.putSerializable(PHOTO_KEY,photoFile);

        PictureFragment fragment = new PictureFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        File photoFile =(File)getArguments().getSerializable(PHOTO_KEY);

        View view = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_picture,null);
        image = view.findViewById(R.id.larger_image);

        if (photoFile == null || !photoFile.exists()){
            image.setImageDrawable(null);
        }else {
            Bitmap photo = PictureUtils
                    .getScaledBitmap(photoFile.getPath(),getActivity());
            image.setImageBitmap(photo);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(R.string.larger_picture)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                .create();

    }
}
