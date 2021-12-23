package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class TaskInfo extends AppCompatActivity {

    TextView nameTextView, detailsTextView, assignedToTextView, dateCreatedTextView;
    BottomNavigationView bottomNavigationView;

    ParseObject assignedToObject;

    boolean isAssigned;

    Intent sentTaskInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_info);

        sentTaskInfo = getIntent();

        setBottomNavListener();

        nameTextView = findViewById(R.id.taskNameTextView);
        detailsTextView = findViewById(R.id.detailsTextView);
        assignedToTextView = findViewById(R.id.assignedToTextView);
        dateCreatedTextView = findViewById(R.id.dateTaskCreatedTextView);

        isAssigned = sentTaskInfo.getBooleanExtra("isAssigned", false);

        if(isAssigned){
            ParseQuery<ParseUser> assignedToQuery = ParseUser.getQuery();
            assignedToQuery.whereEqualTo("objectId",sentTaskInfo.getStringExtra("assignedTo"));
            assignedToQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List objects, ParseException e) {
                }

                @Override
                public void done(Object o, Throwable throwable) {
                    Log.d("O", "O");
                    if(throwable == null){
                        Log.d("RECIEVED OBJECT", o.toString());
                        ArrayList<ParseUser> parseUsersArrayList = (ArrayList<ParseUser>) o;
//                        Log.d("size of Object o", Integer.toString(parseUsersArrayList.size()));
                        assignedToObject = (ParseUser) parseUsersArrayList.get(0);
                        assignedToTextView.setText("assigned to: "+assignedToObject.get("name").toString());
                    }
                }
            });
        }

        if(!isAssigned){
            assignedToTextView.setText("No one has been assigned to this task..");
        }

        nameTextView.setText("task: "+sentTaskInfo.getStringExtra("Name"));
        detailsTextView.setText("details: "+sentTaskInfo.getStringExtra("details"));
        dateCreatedTextView.setText("date created: "+sentTaskInfo.getStringExtra("dateTaskCreated"));
    }
    //
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
    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeObjectID", sentTaskInfo.getStringExtra("HomeObjectID"));
        startActivity(switchActivity);
    }
}