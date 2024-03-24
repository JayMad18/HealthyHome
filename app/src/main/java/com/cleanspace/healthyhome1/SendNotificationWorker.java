package com.cleanspace.healthyhome1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
public class SendNotificationWorker extends Worker {

    private final String FCM_API = getApplicationContext().getString(R.string.FCM_API);
    private final String server_key = getApplicationContext().getString(R.string.server_key);
    private final String content_type = getApplicationContext().getString(R.string.content_type);

    String taskName;
    String taskDetails;
    long alarmTimeMillis;

    String homeObjectId;

    public SendNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        /*details for alarm to be created on the device that receives this notoficaiton*/
        taskName = getInputData().getString("taskName");
        taskDetails = getInputData().getString("taskDetails");
        alarmTimeMillis = getInputData().getLong("alarmTimeMillis", 10000);
        homeObjectId = getInputData().getString("HomeObjectID");

        //details for notification
        String title = getInputData().getString("title");
        String message = getInputData().getString("message");
        Boolean isAssigned = getInputData().getBoolean("isAssigned", false);
        String PERSONALTOPIC = getInputData().getString("PERSONALTOPIC");
        String TOPIC = getInputData().getString("TOPIC");


        sendNotification(buildJSONMessageObject(title, message, isAssigned, PERSONALTOPIC, TOPIC));
        //sendBroadcastToCreateAlarm(getApplicationContext(), "task name", "taskDetails", 10000);
        return Result.success();
    }
//    public void sendBroadcastToCreateAlarm(Context context, String taskName, String taskDetails, long alarmTimeMillis){
//        /*TODO: Need to send intent data to tell if alarm is reoccuring or not, should do this after getting one time alarm functional*/
//        Intent intent = new Intent("com.example.taskalarm.ALARM_TRIGGERED");
//        intent.putExtra("taskName", taskName);
//        intent.putExtra("taskDetails", taskDetails);
//        intent.putExtra("alarmTimeMillis", alarmTimeMillis);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
//    }

    public JSONObject buildJSONMessageObject(String title, String message, Boolean isAssigned, String PERSONALTOPIC, String TOPIC){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try {
            notificationBody.put("title", title);
            notificationBody.put("message", message);
            notificationBody.put("taskName", taskName);
            notificationBody.put("taskDetails", taskDetails);
            notificationBody.put("alarmTimeMillis", alarmTimeMillis);
            notificationBody.put("HomeObjectID",homeObjectId);
            notificationBody.put("topic", TOPIC);
            if(isAssigned){
                notificationBody.put("personalTopic",PERSONALTOPIC);
                notification.put("to", PERSONALTOPIC);
            }
            else{
                notification.put("to", TOPIC);
            }

            //notification.put("registration_ids",registrationTokens);
            notification.put("data", notificationBody);
        } catch ( JSONException e) {
            if(isAssigned){
                logToast("JSONException isAssigned = true", e.getLocalizedMessage());
            }
            else{
                logToast("JSONException isAssigned = false", e.getLocalizedMessage());
            }

        }
        return notification;
    }

    /*
    * Send using the FCM legacy HTTP API*/
    public void sendNotification(JSONObject notification) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        logToast("----JSON notification sent to FCM----", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logToast("FCM--ERROR--Response", error.getLocalizedMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + server_key);
                params.put("Content-Type", content_type);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    public void logToast(String tag, String text) {
        Log.d(tag, text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }
//    public void scheduleTask(){
//        //logToast("scheduleTask()"," called");
//
//        Calendar c = getCalendar();
//
//        if(dateOrTimeSet){
//            PendingIntent pendingIntent = putIntentData();
//
//            AlarmManager alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
//            //alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
//            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(c.getTimeInMillis(),pendingIntent);
//            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);
//        }
//        else{
//            Toast.makeText(getApplicationContext(),"No time or date has been set for the task",Toast.LENGTH_SHORT).show();
//            Toast.makeText(getApplicationContext(),"dateOrTimeSet: "+ dateOrTimeSet,Toast.LENGTH_SHORT).show();
//        }
//        changeActivity(HomeScreen.class);
//
//
//
//        //use this to cancel an intent
//        // PendingIntent.getService(this.getApplicationContext(),234324243,intent,PendingIntent.FLAG_NO_CREATE);
//    }
}
