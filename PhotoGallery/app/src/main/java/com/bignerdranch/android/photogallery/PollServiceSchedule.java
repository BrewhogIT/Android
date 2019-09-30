package com.bignerdranch.android.photogallery;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class PollServiceSchedule extends JobService {
    private Context mContext = this;
    private final String TAG = "PollServiceSchedule";
    private publicNotification publicNotification;


    @Override
    public boolean onStartJob(JobParameters params) {
        Log.i(TAG, "onStartJob is start");

        publicNotification = new publicNotification();
        publicNotification.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    public static void setPollServiceSchedule (Context  context,boolean isOn){
        final int JOB_ID = 1;
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (isOn) {
            ComponentName serviceName = new ComponentName(context,
                    PollServiceSchedule.class);
            JobInfo jobInfo = new JobInfo.Builder(JOB_ID,serviceName)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(PollService.POLL_INTERVAL_MS)
                    .setPersisted(true)
                    .build();
            scheduler.schedule(jobInfo);
        } else {
            scheduler.cancel(JOB_ID);
        }
    }

    public static boolean isJobPlanned(Context context){
        final int JOB_ID = 1;
        JobScheduler scheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        boolean hasBeenSchedualed = false;
        for (JobInfo jobInfo: scheduler.getAllPendingJobs()){
            if (jobInfo.getId() == JOB_ID){
                hasBeenSchedualed = true;
            }
        }

        return hasBeenSchedualed;
    }

    private class publicNotification extends AsyncTask<JobParameters,Void,Void>{
        private static final String TAG = "publicNotification";
        private final String CHANEL_ONE ="chanel one";

        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            JobParameters parameter = jobParameters[0];

            String query = QueryPreferences.getStoredQuery(mContext);
            String lastResultId = QueryPreferences.getLastResultId(mContext);
            List<GalleryItem> items;

            if (query == null){
                items = new FlickrFetchr().fetchRecentPhotos();
            }else{
                items = new FlickrFetchr().searchPhotos(query);
            }

            if (items.size() == 0){
                return null;
            }
            String resultId = items.get(0).getId();
            if (resultId.equals(lastResultId)){
                Log.i(TAG,"Got an old result " + resultId);
            }else{
                Log.i(TAG,"Got a new result " + resultId);

                Resources resources = getResources();
                Intent i = PhotoGalleryActivity.newIntent(mContext);
                PendingIntent pi = PendingIntent.getActivity(mContext,0,i,0);

                Notification notification = new NotificationCompat.Builder(mContext,CHANEL_ONE)
                        .setTicker(resources.getString(R.string.new_pictures_title))
                        .setSmallIcon(android.R.drawable.ic_menu_report_image)
                        .setContentTitle(resources.getString(R.string.new_pictures_title))
                        .setContentText(resources.getString(R.string.new_pictures_text))
                        .setContentIntent(pi)
                        .setAutoCancel(true)
                        .build();

                NotificationManagerCompat notificationManager =
                        NotificationManagerCompat.from(mContext);
                notificationManager.notify(0,notification);
            }

            QueryPreferences.setLastResultId(mContext,resultId);

            jobFinished(parameter,false);
            return null;
        }
    }
}
