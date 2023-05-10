package com.cleanspace.healthyhome1;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SendRequestAnswerWorker extends Worker {
    private final String FCM_API = getApplicationContext().getString(R.string.FCM_API);
    private final String server_key = getApplicationContext().getString(R.string.server_key);
    private final String content_type = getApplicationContext().getString(R.string.content_type);
    String foundHomeObjectID,requestedUserObjectID, topic, personalTopic;

    Boolean isAccepted;
    ParseUser user;
    ParseObject home;

    ArrayList<String> membersList = new ArrayList<String>();
    ArrayList<String> homesList = new ArrayList<String>();

    public SendRequestAnswerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);



    }

    @NonNull
    @Override
    public Result doWork() {
        try{
            foundHomeObjectID = getInputData().getString("foundHomeObjectID");
            logToast("SendRequestAnswerWorker.java -> doWork(): ",  foundHomeObjectID+ "-----------------\n");
            requestedUserObjectID = getInputData().getString("requestedUserObjectID");
            logToast("SendRequestAnswerWorker.java -> doWork(): ",  requestedUserObjectID+ "-----------------\n");
            topic = getInputData().getString("topic");
            logToast("SendRequestAnswerWorker.java -> doWork(): ",  topic+ "-----------------\n");
            personalTopic = getInputData().getString("personalTopic");
            logToast("SendRequestAnswerWorker.java -> doWork(): ",  personalTopic+ "-----------------\n");
            isAccepted = getInputData().getBoolean("isAccepted",false);

            //getUser();
        }catch(Exception e){
            logToast("Error SendRequestAnswerWorker.java -> doWork(): ", e.getLocalizedMessage() + "-----------------");
        }
        sendNotification(buildJSONMessageObject());
        return Result.success();
    }

    public JSONObject buildJSONMessageObject(){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try {
            if(isAccepted){
                notificationBody.put("title", "Accepted!");
                notificationBody.put("message", "A member has Accepted your request to join their home!");
            }
            else{
                notificationBody.put("title", "Denied..");
                notificationBody.put("message", "A member has rejected your request to join their home..");
            }

            notificationBody.put("HomeObjectID", foundHomeObjectID);
            notificationBody.put("requestedUserObjectID", requestedUserObjectID);
            notificationBody.put("topic", topic);
            notificationBody.put("personalTopic",personalTopic);
            notification.put("to", personalTopic);


            //notification.put("registration_ids",registrationTokens);
            notification.put("data", notificationBody);
        } catch ( JSONException e) {
            logToast("JSONException isAssigned = true", e.getLocalizedMessage());
        }
        return notification;
    }
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

    }
}
