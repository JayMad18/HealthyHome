package com.cleanspace.healthyhome1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkRequest;
import androidx.work.impl.utils.ForceStopRunnable; //assuming we dont want to force stop the listening for the intent to create an alarm, we will not use ForceStopRunnable

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    String taskName;
    String taskDetails;
    long alarmTimeMillis;
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.taskalarm.ALARM_TRIGGERED")) {
            // Get the task name and details from the intent
             taskName = intent.getStringExtra("taskName");
             taskDetails = intent.getStringExtra("taskDetails");
             alarmTimeMillis = intent.getLongExtra("alarmTimeMillis", 0);

             CreateAlarmWorkRequest();

            // Display a notification or trigger an alarm
            // You can use the same code as in the previous example here
        }
    }

    public void CreateAlarmWorkRequest(){
        WorkRequest CreateAlarmWorkRequest = new OneTimeWorkRequest.Builder(
                CreateAlarmWorker.class).setInputData(new Data.Builder().putString("taskName", taskName).putString("taskDetails", taskDetails).putLong("alarmTimeMilli", alarmTimeMillis).build()).build();
    }
}
