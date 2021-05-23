package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;

import java.util.List;

public class Homes extends AppCompatActivity {
    BottomNavigationView logout;
    ParseObject foundHomeObject;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homes);
        setLogoutListener();

    }
    public void createNewHome(View view){
        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
        homeQuery.whereEqualTo("Members", ParseUser.getCurrentUser());
        homeQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                //if no objects found matching query then method throws exception
                if(e == null){
                    //Homes containing member was found
                    Toast.makeText(getApplicationContext(),"User is only allowed one home temporarily", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(e.getLocalizedMessage().equals("no results found for query")){
                        changeActivity(CreateHome.class);
                    }
                    else{
                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        Log.i("Exeption thrown", e.getLocalizedMessage());
                    }
                }
            }
        });

    }

    public void searchHome(View view){
        TextView resultsView = findViewById(R.id.searchResults);
        EditText searchExistingHomeEditText = findViewById(R.id.searchExistingHomeEditText);

        ParseQuery<ParseObject> idQuery = ParseQuery.getQuery("Homes");
        idQuery.whereEqualTo("ID",searchExistingHomeEditText.getText().toString());
        idQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    Toast.makeText(getApplicationContext(),"Home or Homes found matching ID", Toast.LENGTH_SHORT).show();
                    String foundHome = "";
                    foundHomeObject = objects.get(0);
                    for(ParseObject object: objects){
                        foundHome += object.getString("HomeName") + "\n";
                    }
                    resultsView.setText(foundHome);
                    resultsView.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    Log.i("Error searching home ID", e.getLocalizedMessage());
                }
            }
        });

    }

    public void viewHome(View view){
        //TODO: go to "ViewHome" activity to view home details and send join request.
        EditText searchExistingHomeEditText = findViewById(R.id.searchExistingHomeEditText);
        Intent viewHome = new Intent(getApplicationContext(), ViewHome.class);
        viewHome.putExtra("HomeId", searchExistingHomeEditText.getText().toString());
        startActivity(viewHome);
    }

    public void setLogoutListener(){
        logout = findViewById(R.id.bottom_navigation);
        logout.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
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

    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
}