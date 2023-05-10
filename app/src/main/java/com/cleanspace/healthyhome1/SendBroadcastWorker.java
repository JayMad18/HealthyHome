package com.cleanspace.healthyhome1;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class SendBroadcastWorker extends Worker {

    public SendBroadcastWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);

    }
    @NonNull
    @Override
    public Result doWork() {

        sendBroadcastToCreateAlarm(getApplicationContext(), "task name", "taskDetails", 10000);
        return Result.success();
    }
    public void sendBroadcastToCreateAlarm(Context context, String taskName, String taskDetails, long alarmTimeMillis){
        Intent intent = new Intent("com.example.taskalarm.ALARM_TRIGGERED");
        intent.putExtra("taskName", taskName);
        intent.putExtra("taskDetails", taskDetails);
        intent.putExtra("alarmTimeMillis", alarmTimeMillis);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
