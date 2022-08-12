package com.cleanspace.healthyhome1;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.parse.ParseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.ALARM_SERVICE;
import static com.parse.Parse.getApplicationContext;


public class HealthyHomeBroadcastReceiverOne extends BroadcastReceiver {

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using


     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     *
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b> This means you should not perform any operations that
     * return a result to you asynchronously. If you need to perform any follow up
     * background work, schedule a {@link JobService} with
     * {@link JobScheduler}.
     * <p>
     * If you wish to interact with a service that is already running and previously

     * you can use {@link #peekService}.
     *
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */

    Intent intent;

    //Send final variables by intent.
    //this below will be visible on github VERY BAD
    private final String FCM_API = "";
    private final String server_key = "";
    private final String content_type = "";

    String taskName, taskDetails, assignedToObjectId, TOPIC, PERSONALTOPIC;
    @Override
    public void onReceive(Context context, Intent intent) {
       logToast("onReceive()","intent received to broadcast");
        Boolean isRepeating = intent.getBooleanExtra("isReoccurring", false);
        this.intent = intent;
        taskName = intent.getStringExtra("taskName");
        taskDetails = intent.getStringExtra("taskDetails");
        TOPIC = intent.getStringExtra("TOPIC");
        PERSONALTOPIC = intent.getStringExtra("PERSONALTOPIC");
        if(intent.getStringExtra("isAssigned").equals("true")){
            assignedToObjectId = intent.getStringExtra("assignToObjectId");

            buildJSONMessageObject(true);
        }
        else if(intent.getStringExtra("isAssigned") == null){
            Toast.makeText(getApplicationContext(),"isAssigned is null in BR", Toast.LENGTH_LONG).show();
            Log.d("BroadcastReciever", "isAssigned is null in BR");
        }
        else{

            buildJSONMessageObject(false);
        }

        if(isRepeating){
            Toast.makeText(context, "Repeating task", Toast.LENGTH_LONG).show();
            reSchedule();
        }
        else{
            Toast.makeText(context, "One time task", Toast.LENGTH_LONG).show();
        }

        PendingIntent.getService(getApplicationContext(),234324243,intent,PendingIntent.FLAG_NO_CREATE);
    }

    public void reSchedule(){

        Intent newIntent = new Intent(getApplicationContext(), HealthyHomeBroadcastReceiverOne.class);

        intent.putExtra("taskName", intent.getStringExtra("taskName"));
        intent.putExtra("taskDetails",intent.getStringExtra("taskDetails"));
        intent.putExtra("isAssigned", intent.getStringExtra("isAssigned"));
        if(intent.getStringExtra("isAssigned").equals("true")){
            intent.putExtra("assignToObjectId", intent.getStringExtra("assignToObjectId"));
            intent.putExtra("PERSONALTOPIC", intent.getStringExtra("PERSONALTOPIC"));
        }
        intent.putExtra("TOPIC", intent.getStringExtra("TOPIC"));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 234324243, newIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
        Calendar c = GregorianCalendar.getInstance();
        c.set(Integer.parseInt(intent.getStringExtra("year")),Integer.parseInt(intent.getStringExtra("month")),Integer.parseInt(intent.getStringExtra("dayOfMonth")),Integer.parseInt(intent.getStringExtra("hourOfDay")),Integer.parseInt(intent.getStringExtra("minute")));
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),pendingIntent);
        Toast.makeText(getApplicationContext(), "Alarm set", Toast.LENGTH_LONG).show();
    }

    /*
     *create a JsonObject of the notification body
     * This object will contain the receiver’s topic, notification title,
     * notification message, and other key/value pairs to add.
     * */
    public void buildJSONMessageObject(boolean isAssigned){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try {
            notificationBody.put("title", taskName);
            notificationBody.put("message", taskDetails);
            if(isAssigned){
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

        sendNotification(notification);
    }

    /*
     *make a network request to FCM server using Volley library,
     *then the server will use the request parameters to route the
     *notification to the targeted device.
     * */
    public void sendNotification(JSONObject notification){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        logToast("HHBCRO.activity JsonObjectRequestResponse", response.toString());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                logToast("HHBCRO.activity JsonObjectRequest--ERROR--Response", error.getLocalizedMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization","key="+server_key);
                params.put("Content-Type", content_type);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
    public void logToast(String tag, String text){
        Log.d(tag,text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }
}
