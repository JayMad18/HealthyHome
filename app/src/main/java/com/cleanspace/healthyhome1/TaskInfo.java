package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.DeleteCallback;
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

public class TaskInfo extends AppCompatActivity {

    TextView nameTextView, detailsTextView, assignedToTextView, dateCreatedTextView,
            statusTextView, completedByTextView;
    BottomNavigationView bottomNavigationView;

    ParseObject assignedToObject;
    String taskObjectId;
    String TOPIC;
    ParseUser completedByUserObject;

    ParseObject task;

    boolean isAssigned;

    Intent sentTaskInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        sentTaskInfo = getIntent();
        TOPIC = "/topics/"+sentTaskInfo.getStringExtra("HomeObjectID") + "TOPIC";

        setBottomNavListener();

        nameTextView = findViewById(R.id.taskNameTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        assignedToTextView = findViewById(R.id.assignedToTextView);
        dateCreatedTextView = findViewById(R.id.dateTaskCreatedTextView);
        statusTextView = findViewById(R.id.statusTextView);
        completedByTextView = findViewById(R.id.completedByTextView);

        taskObjectId = sentTaskInfo.getStringExtra("taskObjectId");
        isAssigned = sentTaskInfo.getBooleanExtra("isAssigned", false);

        nameTextView.setText("Task: "+sentTaskInfo.getStringExtra("Name"));
        detailsTextView.setText("details: "+sentTaskInfo.getStringExtra("details"));
        dateCreatedTextView.setText("date created: "+sentTaskInfo.getStringExtra("dateTaskCreated"));



        retrieveTaskObject(taskObjectId);
    }

    public void retrieveTaskStatus(){
        if(isAssigned){
            ParseQuery<ParseUser> assignedToQuery = ParseUser.getQuery();
            assignedToQuery.whereEqualTo("objectId",sentTaskInfo.getStringExtra("assignedTo"));
            assignedToQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List objects, ParseException e) {
                }

                @Override
                public void done(Object o, Throwable throwable) {
                    if(throwable == null){
                        ArrayList<ParseUser> parseUsersArrayList = (ArrayList<ParseUser>) o;
                        assignedToObject = (ParseUser) parseUsersArrayList.get(0);
                        assignedToTextView.setText("assigned to: "+assignedToObject.get("name").toString());

                        if(sentTaskInfo.getStringExtra("isCompleted").equals("true")){
                            statusTextView.setText("Completed");
                            statusTextView.setTextColor(getColor(R.color.DeepPink));

                            completedByTextView.setText("Completed by "+ assignedToObject.get("name").toString());
                        }
                        else if(sentTaskInfo.getStringExtra("isCompleted").equals("false")){
                            statusTextView.setText("Incomplete task");
                            statusTextView.setTextColor(getColor(R.color.Pink));
                            completedByTextView.setText("");
                        }
                    }
                    else{
                        statusTextView.setText("something went ");
                        completedByTextView.setText("wrong..");
                    }
                }
            });
        }
        else{
            assignedToTextView.setText("No one has been assigned to this task..");

            if(sentTaskInfo.getStringExtra("isCompleted").equals("true")){
                ParseQuery<ParseUser> completedByQuery = ParseUser.getQuery();
                completedByQuery.whereEqualTo("objectId", task.get("completedBy").toString());
                completedByQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> objects, ParseException e) {
                        completedByUserObject = (ParseUser) objects.get(0);
                        completedByTextView.setText("Completed by "+ completedByUserObject.get("name"));
                        statusTextView.setText("Completed");
                        statusTextView.setTextColor(getColor(R.color.DeepPink));

                    }
                });

            }
            else if(sentTaskInfo.getStringExtra("isCompleted").equals("false")){
                statusTextView.setText("Incomplete task");
                statusTextView.setTextColor(getColor(R.color.Pink));
                completedByTextView.setText("");
            }
        }
    }

    public void retrieveTaskObject(String taskObjectId){
        ParseQuery<ParseObject> taskQuery = ParseQuery.getQuery("Tasks");
        taskQuery.whereEqualTo("objectId", taskObjectId);
        taskQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    task = objects.get(0);
                    retrieveTaskStatus();
                }
                else{
                    Log.d("retrieveTaskObject()",e.getLocalizedMessage());
                }
            }
        });
    }

    public void setBottomNavListener(){
        String sender = sentTaskInfo.getStringExtra("sender");
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.backItem){
                    if( sender.equals("MyTasks")){
                        changeActivity(MyTasks.class);
                    }
                    else{
                        changeActivity(AllTasks.class);
                    }
                }
                return false;
            }
        });
    }
    public void completeTask(View button){
        if(isAssigned){
            if(ParseUser.getCurrentUser().getObjectId().equals(assignedToObject.getObjectId())){
                new AlertDialog.Builder(this).setTitle("Complete Task").setMessage("Are you sure that this task is complete?")
                        .setPositiveButton("Yea", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                complete();
                            }
                        }).setNegativeButton("Not yet..", null).show();
            }
            else{
                Toast.makeText(getApplicationContext(),"Only the member assigned to this task may mark task completed",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            new AlertDialog.Builder(this).setTitle("Complete Task").setMessage("Are you sure that this task is complete?")
                    .setPositiveButton("Yea", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            complete();
                        }
                    }).setNegativeButton("Not yet..", null).show();
        }

    }

    public void deleteTask(View button){
        String assignerObjectId = task.get("assignerObjectId").toString();
        if(assignerObjectId.equals(ParseUser.getCurrentUser().getObjectId())){
            new AlertDialog.Builder(this).setTitle("Delete Task").setMessage("Are you sure you want to delete this task? ")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(assignerObjectId.equals(ParseUser.getCurrentUser().getObjectId())){
                                delete();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),"Only the member who assigned the task may delete the task",Toast.LENGTH_SHORT).show();
                            }

                        }
                    }).setNegativeButton("Cancel", null).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Only the member who assigned the task may delete the task",Toast.LENGTH_SHORT).show();
        }

    }
    public void complete(){
        if(isAssigned){
            if(ParseUser.getCurrentUser().getObjectId().equals(assignedToObject.getObjectId())){
                task.put("isCompleted",true);
                task.put("completedBy",assignedToObject.getObjectId());
                task.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if(e == null){
                            statusTextView.setText("Completed");
                            statusTextView.setTextColor(getColor(R.color.HotPink));
                            completedByTextView.setText("Completed by "+assignedToObject.get("name").toString());
                            buildJSONMessageObject();                        }
                        else{
                            Toast.makeText(getApplicationContext(),"Failed marking task complete "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
            }
            else{
                Toast.makeText(getApplicationContext(),"Only the member assigned to this task may mark task completed",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            task.put("isCompleted",true);
            task.put("completedBy",ParseUser.getCurrentUser().getObjectId());
            task.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Toast.makeText(getApplicationContext(),"Task marked complete!", Toast.LENGTH_SHORT).show();
                        statusTextView.setText("Completed");
                        completedByTextView.setText("Completed by "+ParseUser.getCurrentUser().get("name").toString());
                        statusTextView.setTextColor(getColor(R.color.HotPink));
                        buildJSONMessageObject();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"Failed marking task complete "+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }
    public void delete(){
        task.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(getApplicationContext(),"Task deleted succesfully", Toast.LENGTH_SHORT).show();
                    changeActivity(HomeScreen.class);
                }
                else {
                    Toast.makeText(getApplicationContext(),"Error deleting task: "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }

    /*
     *create a JsonObject of the notification body
     * This object will contain the receiver’s topic, notification title,
     * notification message, and other key/value pairs to add.
     * */
    public void buildJSONMessageObject(){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        if(isAssigned == true){
            try {
                notificationBody.put("title", "Task completed!");
                notificationBody.put("message", ParseUser.getCurrentUser().getUsername()+" has marked " +
                        task.get("Name").toString() +" completed!");

                notification.put("to", TOPIC);
                //notification.put("registration_ids",registrationTokens);
                notification.put("data", notificationBody);
            } catch ( JSONException e) {
            }
        }
        else{
            try {
                notificationBody.put("title", "New Task");
                notificationBody.put("message", ParseUser.getCurrentUser().getUsername()+" has created a new task: " + task.get("Name"));

                notification.put("to", TOPIC);
                //notification.put("registration_ids",registrationTokens);
                notification.put("data", notificationBody);
            } catch ( JSONException e) {
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
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(getString(R.string.FCM_API), notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization","key="+getString(R.string.server_key));
                params.put("Content-Type", getString(R.string.content_type));
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }


    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeObjectID", sentTaskInfo.getStringExtra("HomeObjectID"));
        startActivity(switchActivity);
    }
}