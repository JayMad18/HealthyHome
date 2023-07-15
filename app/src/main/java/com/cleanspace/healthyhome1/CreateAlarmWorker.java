package com.cleanspace.healthyhome1;

import static android.content.Context.ALARM_SERVICE;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class CreateAlarmWorker extends Worker {

    public CreateAlarmWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("ObsoleteSdkInt")
    @NonNull
    @Override
    public Result doWork() {
        String taskName = getInputData().getString("taskName");
        String taskDetails = getInputData().getString("taskDetails");
        long alarmTimeMillis = getInputData().getLong("alarmTimeMillis", 10000);
        /*
        * TODO: use the year,month,dayOfMonth, and time to create new calender instance and set the date and time and pass to alarManager as cal.getTimeInMillis()
        * */

        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDetails", taskDetails);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarmTimeMillis, pendingIntent);
        }

        return Result.success();
    }
}
