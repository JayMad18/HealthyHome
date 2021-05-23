package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;

public class CreateHome extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    public Boolean isUnique;
    public ArrayList<String> currentExistingHomeIDs = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_home);
        setLogoutListener();
    }

    public void saveHomeToDataBase(String id) throws JSONException {
        EditText homeName = findViewById(R.id.homeNameEditText);
        ParseObject home = new ParseObject("Homes");
        ParseUser user = ParseUser.getCurrentUser();


        //creates an arraylist and adds the current active user then adds arraylist to MembersList column
        ArrayList<String> membersList = new ArrayList<String>();
        membersList.add(user.getObjectId());

        home.put("MembersList", membersList);
        home.put("HomeName",homeName.getText().toString());
        home.put("ID", id);

        home.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.i("Error saving Members to array", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                else {
                    saveHomeToArrayList(user, home);
                }
            }
        });

        //Trying to save an arraylist of homes for the user here but keeps returning null
        //confusing because when I run it on the debugger it works fine and saves the home ObjectId to the arraylist column

        //SOLUTION!!It was because the User data was saving in the background before the Home data got saved, so
        //all I had to do was put User save process inside done() method of saveinbackground for home.
    }

    public void saveHomeToArrayList(ParseUser user, ParseObject home){
        ArrayList<String> homesList = (ArrayList) user.getList("HomeList");
        homesList.add(home.getObjectId());
        user.put("HomeList",homesList);

        EditText homeName = findViewById(R.id.homeNameEditText);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.i("Error saving homesList to HomeList column", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),homeName.getText().toString() +" created succesfully!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void generateUniqueId(View view){
        String id = generateID();

        ParseQuery<ParseObject> IdQuery = ParseQuery.getQuery("Homes");
       IdQuery.whereEqualTo("ID", id);
       IdQuery.findInBackground(new FindCallback<ParseObject>() {
           @Override
           public void done(List<ParseObject> objects, ParseException e) {
               if(e == null){
                   if(objects.size() == 0){
                       try {
                           saveHomeToDataBase(id);
                       } catch (JSONException jsonException) {
                           jsonException.printStackTrace();
                       }
                   }
                   else{
                       for(ParseObject object: objects){
                           Log.i("Id that was caught from query", object.getString("ID"));
                       }
                       generateUniqueId(view);
                   }
               }
               else{
                   Log.i("Error @ IdQuery", e.getLocalizedMessage());
               }
           }
       });

    }

    public String generateID(){
        String IDstring = "";
        Random random = new Random();

        for(int i = 0; i < 4; i++){
            IDstring += Integer.toString(random.nextInt(10));
        }
        return IDstring;
    }

    public void setLogoutListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.logoutItem){
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_SHORT).show();
                                changeActivity(MainActivity.class);
                            }else{
                                Log.i("ERROR!!!!!!", e.getLocalizedMessage());
                                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                return false;
            }
        });
    }

    public void goBack(View view){
        changeActivity(Homes.class);
    }

    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
}