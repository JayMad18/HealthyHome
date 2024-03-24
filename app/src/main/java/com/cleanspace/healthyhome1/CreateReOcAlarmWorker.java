package com.cleanspace.healthyhome1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;

public class CreateReOcAlarmWorker extends Worker {
    public CreateReOcAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        /*
        *
        * TODO: Store each user's alarm request codes so that they can cancel a alaram/reoccurring alarm.
        *  Perhaps store request code for each user via Room local data persistence?
        *
        * TODO: use the year,month,dayOfMonth, and time to create new calender instance and set the date and time and pass to alarManager as cal.getTimeInMillis()
        *
        * TODO: May need to generate unique request code for each user's alarm so that user can delete their alarm via (Room persistence)
        *
        * */



        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 101, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, //using time since system boot up instead of real time clock "scaling issues"
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HALF_HOUR, //saying fire alarm thirty minutes from now
                AlarmManager.INTERVAL_HALF_HOUR, //fire every thirty minutes
                pendingIntent);//pending intent for AlarmReceiver
        return null;
    }
}
