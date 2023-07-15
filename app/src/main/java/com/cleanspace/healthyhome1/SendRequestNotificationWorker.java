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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SendRequestNotificationWorker extends Worker {
    private final String FCM_API = getApplicationContext().getString(R.string.FCM_API);
    private final String server_key = getApplicationContext().getString(R.string.server_key);
    private final String content_type = getApplicationContext().getString(R.string.content_type);
    String foundHomeObjectID, requestedUserObjectID, topic, personalTopic;
    ArrayList<String> membersList = new ArrayList<String>();
    ArrayList<String> homesList = new ArrayList<String>();

    public SendRequestNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        foundHomeObjectID = getInputData().getString("foundHomeObjectID");
        requestedUserObjectID = getInputData().getString("requestedUserObjectID");
        topic = getInputData().getString("topic");
        personalTopic = getInputData().getString("personalTopic");
        //membersList.addAll(Arrays.asList(Objects.requireNonNull(getInputData().getStringArray("membersList"))));
        //homesList.addAll(Arrays.asList(Objects.requireNonNull(getInputData().getStringArray("homesList"))));



        sendNotification(buildJSONMessageObject());
        return Result.success();
    }
    public JSONObject buildJSONMessageObject(){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();

        try {
            notificationBody.put("title", "Request");
            notificationBody.put("message", "A user has requested to join your home!");
            notificationBody.put("HomeObjectID", foundHomeObjectID);
            notificationBody.put("requestedUserObjectID", requestedUserObjectID);
            notificationBody.put("topic", topic);
            notificationBody.put("personalTopic",personalTopic);
            notification.put("to", topic);


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
