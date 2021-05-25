package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyHomes extends AppCompatActivity {
ListView myHomesListView;
ArrayList<String> homeObjects = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_homes);
        myHomesListView = findViewById(R.id.myHomesListView);
        loadHomes();
    }
    public void loadHomes(){
        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
        homeQuery.whereContains("MembersList", ParseUser.getCurrentUser().getObjectId());
        homeQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(ParseUser.getCurrentUser() == null){
                        Toast.makeText(getApplicationContext(),"getCurrentUser = null", Toast.LENGTH_SHORT).show();
                        Log.i("user = null", e.getLocalizedMessage());
                    }
                    else if(objects == null){
                        Toast.makeText(getApplicationContext(),"returned objects list = null", Toast.LENGTH_SHORT).show();
                        Log.i("objects = null", e.getLocalizedMessage());
                    }
                    else{
                        Log.i("objects list size", Integer.toString(objects.size()));
                        for(ParseObject object: objects){
                            homeObjects.add(object.getString("HomeName"));

                        }
                        populateListView();
                    }
                }
                else{
                    Log.i("Error loading homes into memberObjects arraylist", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void populateListView(){
        ArrayAdapter<String> memberObjectsAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,homeObjects);
        memberObjectsAdapter.notifyDataSetChanged();
        myHomesListView.setAdapter(memberObjectsAdapter);
    }
}