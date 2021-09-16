package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class CreateTask extends AppCompatActivity {
    EditText taskNameEditText, userNameEditText;
    ArrayList<ParseUser> membersOfHome = new ArrayList<ParseUser>();

    ListView membersListView;
    String selectedHomeObjectId;
    ParseObject homeObject;
    ArrayList<ParseUser> parseUsers = new ArrayList<ParseUser>();
    ArrayList<String> memberNames = new ArrayList<String>();
    ArrayList<String> memberObjectIds = new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        taskNameEditText = findViewById(R.id.taskNameEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        membersListView = findViewById(R.id.membersListView);

        Intent homeObjectId = getIntent();
        selectedHomeObjectId = homeObjectId.getStringExtra("HomeObjectID");

        //getting homeObject just incase
        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
        homeQuery.getInBackground(selectedHomeObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null){
                    Log.i("query current home", "Home Found!");
                    homeObject = object;
                }
                else {
                    Log.i("query current home", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        //populateListView();
    }
    public void createTask(View view){


        ParseObject task = new ParseObject("Tasks");
        task.put("Name", taskNameEditText.getText().toString());
        task.put("Home", selectedHomeObjectId);
        task.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){

                    Toast.makeText(CreateTask.this, "Task created succesfully", Toast.LENGTH_SHORT).show();
                }
                else{

                    Log.i("error message", e.getLocalizedMessage());
                    Toast.makeText(CreateTask.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        //ParseQuery<ParseUser> loadUsers = ParseUser.getQuery();
    }
    public void populateListView(){
        ArrayAdapter<String> memberNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,memberNames);

        ParseQuery<ParseUser> memberQuery = ParseUser.getQuery();
        memberQuery.whereContains("HomeList", selectedHomeObjectId);
        memberQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    for(ParseUser user: objects){
                        memberNames.add(user.get("name").toString());
                        memberObjectIds.add(user.getObjectId());
                        parseUsers.add(user);
                    }
                    memberNamesAdapter.notifyDataSetChanged();
                    membersListView.setAdapter(memberNamesAdapter);
                }else{
                    Toast.makeText(getApplicationContext(),"Error loading members "+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    Log.d("error loading homes",e.getLocalizedMessage());
                }
            }
        });
    }
}