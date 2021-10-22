package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class MyTasks extends AppCompatActivity {

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
        setContentView(R.layout.activity_my_tasks);

        setBottomNavListener();

        recievedIntent = getIntent();

        selectedHomeObjectId = recievedIntent.getStringExtra("HomeObjectID");
        Log.i("loadUsersTasksForThisHome is being called", "!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        loadTasks();
    }

    public void loadTasks(){
        ParseQuery taskQuery = ParseQuery.getQuery("Tasks");
        taskQuery.whereEqualTo("Home", selectedHomeObjectId);
        taskQuery.whereEqualTo("assignToObjectId", ParseUser.getCurrentUser().getObjectId());
        taskQuery.findInBackground(new FindCallback() {
            @Override
            public void done(List objects, ParseException e) {

            }

            @Override
            public void done(Object o, Throwable throwable) {
                if(throwable == null){

                    parseObjects = (ArrayList<ParseObject>) o;

                    for(ParseObject object : parseObjects){
                        taskList.add(object);
                        taskNames.add(object.get("Name").toString());
                        taskObjectIds.add(object.getObjectId());
                    }
                    populateListView();
                }
                else{
                    Toast.makeText(getApplicationContext(),throwable.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    /**
     * This method below will but put into the MyTasks activity
     *
     *                 still need to update the assigned member's list of task's.
     *                           -solution: will update members list of tasks by
     *                           querying task's containing user's objectId. This will
     *                           take place everytime the member clicks on "MY TASKS"
     *                           button. (Completed)
     *
     *      This "populateListView()" method does all the work when called
     */
    public void populateListView(){
        taskListView = findViewById(R.id.taskListView);
        if(taskNames.size() == 0){
            taskNames.add("Looks like you have nothing to do!");
        }
        ArrayAdapter<String> taskNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,taskNames);
        taskNamesAdapter.notifyDataSetChanged();
        taskListView.setAdapter(taskNamesAdapter);
        updateUsersTaskList();


    }

    //The users tasklist on the parse backend is updated during this activity.
    public void updateUsersTaskList(){
        ParseUser.getCurrentUser().put("taskList", taskObjectIds);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(getApplicationContext(),"task list updated", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
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