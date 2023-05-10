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

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class JSONMiddleMan {
    JSONObject notification;

    public JSONMiddleMan(JSONObject notification) {
        this.notification = notification;
    }

    public JSONObject getNotification() {
        return this.notification;
    }

    public void useWorker() {


    }



}
