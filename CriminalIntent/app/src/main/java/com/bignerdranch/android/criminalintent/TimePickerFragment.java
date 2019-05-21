package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class TimePickerFragment extends DialogFragment {
    private TimePicker timeView;
    private static final String DATE_ARG = "DateArgs";
    public static final String EXTRA_TIME = "com.bignerdranch.android.criminalIntent.time";

    public static TimePickerFragment newInstance(Date date){
        Bundle args = new Bundle();
        args.putSerializable(DATE_ARG,date);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);

        return fragment;
    }

    public static Intent newIntent (Date date){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME,date);
        return intent;
    }

    public void sendResult(int resultCode,Date date){
        if (getTargetFragment() == null){
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME,date);

        getTargetFragment().onActivityResult(getTargetRequestCode(),resultCode,intent);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInsanceState) {
        Date date = (Date) getArguments().getSerializable(DATE_ARG);
        final Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        int hour = calendar.get(Calendar.HOUR);
        int minute = calendar.get(Calendar.MINUTE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time,null);
        timeView = v.findViewById(R.id.dialog_time_picker);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            timeView.setCurrentHour(hour);
            timeView.setCurrentMinute(minute);
        }else{
            timeView.setHour(hour);
            timeView.setMinute(minute);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.time_picker_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour = timeView.getCurrentHour();
                        int minute = timeView.getCurrentMinute();

                        calendar.set(Calendar.MINUTE,minute);
                        calendar.set(Calendar.HOUR,hour);
                        Date newDate = calendar.getTime();

                        sendResult(Activity.RESULT_OK,newDate);
                    }
                })
                .create();
    }
}
