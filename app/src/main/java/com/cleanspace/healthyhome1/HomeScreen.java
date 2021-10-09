package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class HomeScreen extends AppCompatActivity {
    TextView quoteView;
    BottomNavigationView bottomNavigationView;
    ParseObject selectedHome;

    Intent sentHome;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        sentHome = getIntent();
        generateQuote();
        setBottomNavListener();
        retrieveSelectedHome();
    }

    /**
     * This method below will but put into the MyTasks activity
     * TODO **TODO** still need to update the assigned member's list of task's.
     *                           -solution: will update members list of tasks by
     *                           querying task's containing user's objectId. This will
     *                           take place everytime the member clicks on "MY TASKS"
     *                           button.
     */
//
//    public void loadUsersTasksForThisHome(){
//        ParseQuery taskQuery = ParseQuery.getQuery("Tasks");
//        taskQuery.whereEqualTo("Home", selectedHome.getObjectId());
//        taskQuery.whereEqualTo("assignToObjectId", ParseUser.getCurrentUser().getObjectId());
//        taskQuery.findInBackground(new FindCallback() {
//            @Override
//            public void done(List objects, ParseException e) {
//                if(){
//
//                }
//            }
//
//            @Override
//            public void done(Object o, Throwable throwable) {
//
//            }
//        });
//    }

    /*
     * uses data sent from intent to search for the clicked home and fill in appropriate data for the home
     * FILLS IN THE selectedHome global variable
     * */
    public void retrieveSelectedHome(){
        ParseQuery selectedHomeQuery = ParseQuery.getQuery("Homes");
        selectedHomeQuery.whereEqualTo("objectId", sentHome.getStringExtra("HomeObjectID"));
        selectedHomeQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject object, ParseException e) { }

            @Override
            public void done(Object o, Throwable throwable) {
                if(throwable == null){
                    if(o == null){
                        Toast.makeText(getApplicationContext(),"No Home Found", Toast.LENGTH_SHORT).show();
                    }else{
                        selectedHome = (ParseObject) o;
                        Toast.makeText(getApplicationContext(),selectedHome.get("HomeName").toString(), Toast.LENGTH_SHORT).show();
                    }

                }
                else{
                    Toast.makeText(getApplicationContext(),"Error loading Home" + throwable.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.d("Error loading home", throwable.getLocalizedMessage());
                }
            }
        });
    }

    /*
    * Creates and sends and Intent to start ShowMembers activity
    * Intent includes two Extra's, HomeObjectID and HomeName
    * */
    public void showMembers(View view){
        Intent showMembers = new Intent(getApplicationContext(), ShowMembers.class);
        showMembers.putExtra("HomeObjectID", selectedHome.getObjectId());
        showMembers.putExtra("HomeName",selectedHome.get("HomeName").toString());
        startActivity(showMembers);
    }

    /*
    * Creates and sends an Intent to start CreateTask activity
    * Intent includes one Extra, HomeObjectID
    * */
    public void addTask(View view){
        Intent addTask = new Intent(getApplicationContext(), CreateTask.class);
        addTask.putExtra("HomeObjectID", selectedHome.getObjectId());
        startActivity(addTask);
    }
    //bottom nav item click listener
    public void setBottomNavListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.logoutItem){
                   logoutAlertDialog();
                }
                else if(item.getItemId() == R.id.backItem){
                    changeActivity(HomeScreen.class);
                }
                return false;
            }
        });
    }
    /*
    * A logout alert dialog that ask's if sure want to log out,
    * this needs to be implemented in all activites that allow user to log out
    * */
    public void logoutAlertDialog(){
        new AlertDialog.Builder(this).setTitle("Log out").setMessage("Are you sure you want to log out?")
                .setIcon(android.R.drawable.ic_media_previous).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        }).setNegativeButton("No", null).show();
    }
    //logout in its own method
    public void logout(){
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
    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }

    //Called in onCreate to download random quote with another class on another thread while also creating the activity
    @SuppressLint("SetTextI18n")
    private void generateQuote(){
        quoteView = findViewById(R.id.quoteView);
        DownloadRandomQuote randomQuote = new DownloadRandomQuote();
        try{
            //link to random quote api
            JSONObject jsonData = randomQuote.execute("https://api.quotable.io/random?maxLength=50").get();
            Log.i("JSON data tostring", jsonData.toString());
            String content = jsonData.getString("content");
            String author = jsonData.getString("author");
            quoteView.setText(content + " "+author);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
/*
* This class was made for fun to download a random quote from
* a random quote generator JSON api, I decided to leave it in the app.
* AsyncTask is marked out because it is depreciated but it still works
* */
class DownloadRandomQuote extends AsyncTask<String, Void, JSONObject>{
    @Override
    protected JSONObject doInBackground(String... urls) {
        try{
            URL url = new URL(urls[0]);

            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.connect();

            InputStream inputStream = httpsURLConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            String jsonString = "";
            while(scanner.hasNext()){
                jsonString += scanner.next() + " ";
            }

            try {
                JSONObject jsonData = new JSONObject(jsonString);
                return jsonData;
            }catch (Exception e){
                e.printStackTrace();
            }
        }catch (Exception e) {
            //e.printStackTrace();
            Log.i("Failed at do in background", e.toString());
        }
        return null;
    }
}