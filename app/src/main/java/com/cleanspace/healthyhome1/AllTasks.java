package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class AllTasks extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ListView taskListView;

    String selectedHomeObjectId;
    Intent recievedIntent;

    ArrayList<ParseObject> taskList = new ArrayList<ParseObject>();
    ArrayList<String> taskNames = new ArrayList<String>();
    ArrayList<String> taskObjectIds =new ArrayList<String>();
    ArrayList<ParseObject> parseObjects = new ArrayList<ParseObject>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_tasks);

        setBottomNavListener();

        recievedIntent = getIntent();

        selectedHomeObjectId = recievedIntent.getStringExtra("HomeObjectID");
        populateListView();
        onItemClickListener();
    }

    public void populateListView(){

        ArrayAdapter<String> taskNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,taskNames);
        taskListView = findViewById(R.id.taskListView);

        ParseQuery taskQuery = ParseQuery.getQuery("Tasks");
        taskQuery.whereEqualTo("Home", selectedHomeObjectId);
        taskQuery.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {

            }

            //As far as I understand, the Object o must be a list object since I can cast it to an ArrayList..
            @Override
            public void done(Object o, Throwable throwable) {
                if(throwable == null){

                    parseObjects = (ArrayList<ParseObject>) o;

                    if(parseObjects.size() == 0){
                        taskNames.add("No task's for this home yet..");
                    }
                    else{
                        for(ParseObject object : parseObjects){
                            taskList.add(object);
                            taskNames.add(object.get("Name").toString());
                            taskObjectIds.add(object.getObjectId());

                        }
                    }
                    taskNamesAdapter.notifyDataSetChanged();
                    taskListView.setAdapter(taskNamesAdapter);
                }
                else{
                    Toast.makeText(getApplicationContext(),throwable.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }

            }
        });
    }
    public void onItemClickListener(){
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent sendTaskInfo = new Intent(getApplicationContext(), TaskInfo.class);

                ParseObject selectedTask = taskList.get(position);

                sendTaskInfo.putExtra("Name", selectedTask.get("Name").toString());
                sendTaskInfo.putExtra("details",selectedTask.get("details").toString());
                sendTaskInfo.putExtra("HomeObjectID", selectedTask.get("Home").toString());
                sendTaskInfo.putExtra("isAssigned", selectedTask.getBoolean("isAssigned"));
                if(selectedTask.getBoolean("isAssigned")){
                    sendTaskInfo.putExtra("assignedTo", selectedTask.get("assignToObjectId").toString());
                }
                sendTaskInfo.putExtra("dateTaskCreated", selectedTask.getCreatedAt().toString());
                sendTaskInfo.putExtra("sender", "AllTasks");
                //selected user needs a session token to use .getEmail(), which means only logged in user can use .getEmail()
                startActivity(sendTaskInfo);
            }
        });
    }



    public void setBottomNavListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.backItem){
                    changeActivity(HomeScreen.class);
                }
                return false;
            }
        });
    }
    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeObjectID", selectedHomeObjectId);
        startActivity(switchActivity);
    }

}