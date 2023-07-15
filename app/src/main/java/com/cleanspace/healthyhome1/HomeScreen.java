package com.cleanspace.healthyhome1;

import static com.parse.Parse.getApplicationContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
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
import com.parse.SaveCallback;

import org.json.JSONObject;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class HomeScreen extends AppCompatActivity {
    TextView quoteView, homeNameAndIdTextView;
    BottomNavigationView bottomNavigationView;
    ParseObject selectedHome;
    String selectedHomeObjectId;
    String homeName;
    boolean isNotification = false;
    boolean isRequest = false;

    Intent retrievedIntent;

    ParseUser user;

    View topLeftButton, topRightButton,bottomLeftButton,bottomRightButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        /*
        *
        * */


        user = ParseUser.getCurrentUser();
        homeNameAndIdTextView = findViewById(R.id.homeNameAndIdTextView);
        generateQuote();
        setBottomNavListener();

        if(getIntent() == null){//making sure intent isnt null
            Log.d("ERROR HomeScreen.java -> Received Intent", "---------------Null!!!-------------------");
            Toast.makeText(getApplicationContext(),"No intent recieved!",Toast.LENGTH_LONG).show();
        }else{
            retrievedIntent = getIntent();
            Log.d(" HomeScreen.java -> Received Intent", "--------------- RETREIEVED SUCCESSFULLY!!!-------------------");
            Log.d(" HomeScreen.java.java -> Received Intent:", retrievedIntent.toString() +"--------------");
            if(retrievedIntent.getStringExtra("HomeObjectID") != null){
                //to tell if intent sent from notfication or not by checking if '*' at end of string.
                selectedHomeObjectId = retrievedIntent.getStringExtra("HomeObjectID");
                logToast(" HomeScreen.java -> Received Intent", retrievedIntent.toString() + "-------------------");
                logToast(" HomeScreen.java -> Received home object id from intent", retrievedIntent.getStringExtra("HomeObjectID") + "-------------------");
                if(retrievedIntent.getStringExtra("notification") != null){
                    isNotification = true;
                }
                else if ((retrievedIntent.getStringExtra("Request") != null)) {
                    isRequest = true;
                }
                else
                {
                    logToast("HomeScreen.java -> retreiveSelectedHomeObject", "String extra 'notification' was null----------------------");

                }
                retrieveSelectedHomeObject();
            }
            else{
                logToast(" HomeScreen.java onCreate() -> Received Intent",  "received intent does not contain value for homeobjectid-------------------");

            }
        }

        topLeftButton = findViewById(R.id.topLeftButton);
        topRightButton = findViewById(R.id.topRightButton);
        bottomLeftButton = findViewById(R.id.bottomLeftButton);
        bottomRightButton = findViewById(R.id.bottomRightButton);

    }
    public void askToViewTask(){
        logToast("HomeScreen.java -. askToViewTask()", "called----------------------");
        new AlertDialog.Builder(this).setTitle("View task").setMessage("Do you want to view task?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent myTasks = new Intent(getApplicationContext(), MyTasks.class);
                        myTasks.putExtra("HomeObjectID", selectedHomeObjectId);
                        startActivity(myTasks);
                    }
                }).setNegativeButton("No", null).show();
    }
    public void askToAcceptOrDenyRequest(){
        logToast("HomeScreen.java -. askToAcceptOrDenyRequest()", "called----------------------");
        new AlertDialog.Builder(this).setTitle("Accept member?").setMessage("A user has requested to join your home")
                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        addUserToHomeWorkRequest(retrievedIntent.getStringExtra("HomeObjectID"), retrievedIntent.getStringExtra("requestedUserObjectID")
//                                , retrievedIntent.getStringExtra("topic")
//                                ,retrievedIntent.getStringExtra("personalTopic"));

                        sendRequestAnswerWorkRequest(retrievedIntent.getStringExtra("HomeObjectID"), retrievedIntent.getStringExtra("requestedUserObjectID")
                                , retrievedIntent.getStringExtra("topic")
                                ,retrievedIntent.getStringExtra("personalTopic"), true);


                    }
                }).setNegativeButton("Deny",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        addUserToHomeWorkRequest(retrievedIntent.getStringExtra("HomeObjectID"), retrievedIntent.getStringExtra("requestedUserObjectID")
//                                , retrievedIntent.getStringExtra("topic")
//                                ,retrievedIntent.getStringExtra("personalTopic"));

                        sendRequestAnswerWorkRequest(retrievedIntent.getStringExtra("HomeObjectID"), retrievedIntent.getStringExtra("requestedUserObjectID")
                                , retrievedIntent.getStringExtra("topic")
                                ,retrievedIntent.getStringExtra("personalTopic"), false);


                    }
                }).show();
    }
    public void sendRequestAnswerWorkRequest(String foundHomeObjectID, String requestedUserObjectID, String topic, String personalTopic, boolean isAccepted){
        WorkRequest sendRequestAnswerWorkRequest = new OneTimeWorkRequest.Builder(
                SendRequestAnswerWorker.class).setInputData(new Data.Builder()
                        .putString("foundHomeObjectID", foundHomeObjectID)
                        .putString("requestedUserObjectID", requestedUserObjectID)
                        .putString("topic", topic)
                        .putString("personalTopic", personalTopic)
                        .putBoolean("isAccepted", isAccepted)
                        .build())
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(sendRequestAnswerWorkRequest);
        logToast("HomeScreen.java -> sendRequestAnswerWorker Enqueued!!!", "----------------------------");
    }
    public void addUserToHomeWorkRequest(String foundHomeObjectID, String requestedUserObjectID, String topic, String personalTopic){
        WorkRequest addUserToHomeWorkRequest = new OneTimeWorkRequest.Builder(
                AddUserToHomeWorker.class).setInputData(new Data.Builder()
                        .putString("foundHomeObjectID", foundHomeObjectID)
                        .putString("requestedUserObjectID", requestedUserObjectID)
                        .putString("topic", topic)
                        .putString("personalTopic", personalTopic)
                        .build())
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(addUserToHomeWorkRequest);
        logToast("addUserToHomeWorkRequest Enqueued!!!", "----------------------------");
    }
//    protected void onResume() {
//        super.onResume();
//
//        if(getIntent() == null){//making sure intent isnt null
//            Log.d("getIntent: ", "---------------Null!!!-------------------");
//            Toast.makeText(getApplicationContext(),"No intent recieved!",Toast.LENGTH_LONG).show();
//        }else{
//            retrievedHome = getIntent();
//            Log.d(" HomeScreen.java -> Received Intent", "--------------- RETREIEVED SUCCESSFULLY!!!-------------------");
//            if(retrievedHome.getStringExtra("HomeObjectID") != null){
//                selectedHomeObjectId = retrievedHome.getStringExtra("HomeObjectID");
//                logToast(" HomeScreen.java -> Received Intent", retrievedHome.toString() + "-------------------");
//                logToast(" HomeScreen.java -> Received home object id from intent", retrievedHome.getStringExtra("HomeObjectID") + "-------------------");
//            }
//        }
//    }
    public void logToast(String tag, String text){
        Log.d(tag,text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Make sure to call the super method so that the states of our views are saved
        super.onSaveInstanceState(outState);
        // Save our own state now

    }

    /*
    * Creates and sends and Intent to start ShowMembers activity
    * Intent includes two Extra's, HomeObjectID and HomeName
    * */
    public void showMembers(View view){
        Intent showMembers = new Intent(getApplicationContext(), ShowMembers.class);
        showMembers.putExtra("HomeObjectID", selectedHomeObjectId);
        showMembers.putExtra("HomeName",homeName);
        startActivity(showMembers);
    }

    /*
    * Creates and sends an Intent to start CreateTask activity
    * Intent includes one Extra, HomeObjectID
    * */
    public void addTask(View view){
        Intent addTask = new Intent(getApplicationContext(), CreateTask.class);
        addTask.putExtra("HomeObjectID", selectedHomeObjectId);
        startActivity(addTask);
    }

    public void myTasks(View view){
        Intent myTasks = new Intent(getApplicationContext(), MyTasks.class);
        myTasks.putExtra("HomeObjectID", selectedHomeObjectId);
        startActivity(myTasks);
    }

    public void allTasks(View view){
        Intent allTasks = new Intent(getApplicationContext(), AllTasks.class);
        allTasks.putExtra("HomeObjectID", selectedHomeObjectId);
        startActivity(allTasks);
    }


    /*
     * uses data sent from intent to search for the clicked home ParseObject to easily get info about the home if necessary
     * assigns the selectedHome ParseObject global variable
     *
     * */
    public void retrieveSelectedHomeObject(){
        ParseQuery selectedHomeQuery = ParseQuery.getQuery("Homes");
        selectedHomeQuery.whereEqualTo("objectId", selectedHomeObjectId);
        selectedHomeQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject object, ParseException e) { }

            @Override
            public void done(Object o, Throwable throwable) {
                if(throwable == null){
                    if(o == null){
                    }else{
                        selectedHome = (ParseObject) o;
                        String homeNameAndIdText = selectedHome.get("HomeName").toString()+": " + selectedHome.get("ID");
                        homeName = selectedHome.get("HomeName").toString();
                        homeNameAndIdTextView.setText(homeNameAndIdText);
                        if(isNotification){
                            askToViewTask();
                        } else if (isRequest) {
                            askToAcceptOrDenyRequest();
                        }
                        topLeftButton.setVisibility(View.VISIBLE);
                        topRightButton.setVisibility(View.VISIBLE);
                        bottomLeftButton.setVisibility(View.VISIBLE);
                        bottomRightButton.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    logToast("Error HomeScreen.java -> retrieveSelectedHomeObject()","Throwable does not equal null: " + throwable.getLocalizedMessage() + "-----------------");
                }
            }
        });
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
                    changeActivity(MyHomes.class);
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
        user.put("isLoggedIn",false);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    ParseUser.logOutInBackground(new LogOutCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                changeActivity(MainActivity.class);
                            }else{
                                user.put("isLoggedIn",true);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                    }
                                });
                            }
                        }
                    });
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
            String content = jsonData.getString("content");
            String author = jsonData.getString("author");
            quoteView.setText(content + "\n"+ "-"+author);
        }catch (Exception e){
            e.printStackTrace();
            quoteView.setText("Sorry, no quote available..");
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
            e.printStackTrace();
        }
        return null;
    }

}