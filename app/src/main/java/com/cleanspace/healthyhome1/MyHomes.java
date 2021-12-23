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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class MyHomes extends AppCompatActivity {
ListView myHomesListView;
ArrayList<String> homeObjectsHomeName = new ArrayList<String>();
ArrayList<String> homeObjectsIDs = new ArrayList<String>();
BottomNavigationView bottomNavigationView;
//As you may see my method consolodation starts to improve the further in the activites
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_homes);
        myHomesListView = findViewById(R.id.myHomesListView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loadHomes();
        bottomNavListener();
    }
    //bottomNavView
    public void bottomNavListener(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(item.getItemId() == (R.id.backItem)){
                    changeActivity(Homes.class);
                }
                return false;
            }
        });
    }
    /*
    * queries all homes that contain the current user objectId in its members list.
    * adds home objects names to an arraylist
    * adds home objects id's to an arraylist
    * then calls populateListView even if no homes found.
    * */
    public void loadHomes(){
        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
        homeQuery.whereContains("MembersList", ParseUser.getCurrentUser().getObjectId());
        homeQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    //I dont know why I included this check because the callback would have thrown an exception if ParseUser.getCurrentUser() == null
                    //matter of fact it would not have had any objectId to search.
                    if(ParseUser.getCurrentUser() == null){
                    }//I think these were just over-cautionary checks
                    else if(objects == null){
                    }
                    else{
                        for(ParseObject object: objects){
                            homeObjectsHomeName.add(object.getString("HomeName"));
                            homeObjectsIDs.add(object.getObjectId());

                        }
                        populateListView();
                    }
                }
                else{
                }
            }
        });
    }
    /*
    * populates listview with homeObjectsHomeName
    * calls listViewItemClickListener
    *    This "populateListView()" method has majority of work done in loadHomes() method
    * */
    public void populateListView(){
        if(homeObjectsHomeName.size() == 0){
            homeObjectsHomeName.add("You are not yet apart of any home.. :(");
        }
        ArrayAdapter<String> memberObjectsAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,homeObjectsHomeName);
        memberObjectsAdapter.notifyDataSetChanged();
        myHomesListView.setAdapter(memberObjectsAdapter);
        listViewItemClickListener();
    }
    //listens for a click of an item inside the listview,
    //when item/home clicked, calls goToHomeThatWasSelected which checks to see which home was selected
    public void listViewItemClickListener(){
        myHomesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(homeObjectsHomeName.get(position).equals("You are not yet apart of any home.. :(")){
                    changeActivity(CreateHome.class);
                }
                else{
                    goToHomeThatWasSelected(HomeScreen.class, position);
                }
            }
        });
    }
    //helper method to switch activities quickly
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
    /*
    * Switiches to the HomeScreen activity and sends HomeName and Home objectId as well
    * to be able to fill the HomeScreen with the correct data of clicked home.
    * */
    public void goToHomeThatWasSelected(Class activity, int position){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeName", homeObjectsHomeName.get(position));
        switchActivity.putExtra("HomeObjectID", homeObjectsIDs.get(position));
        startActivity(switchActivity);
    }
}