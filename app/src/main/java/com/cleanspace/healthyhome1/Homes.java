package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class Homes extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    ParseObject foundHomeObject;
    ParseUser user;

    private static final int REQUEST_CODE = 1234;

    //Includes setLogoutListener to listen for logout as soon as activity is created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homes);

        setBottomNavListener();
        retrieveCurrentFCMRegistrationToken();
        user = ParseUser.getCurrentUser();


    }


    /*
    * Switched to the CreateHome activity.
    *
    * */
    public void createNewHome(View view){
        changeActivity(CreateHome.class);

        /**
         * Code below will allow users to only have one home, I believe I may need to use this again
         */
//        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
//        homeQuery.whereEqualTo("Members", ParseUser.getCurrentUser().getObjectId());
//        homeQuery.getFirstInBackground(new GetCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject object, ParseException e) {
//                //if no objects found matching query then method throws exception
//                if(e == null){
//                    //Homes containing member was found
//                }
//                else{
//                    if(e.getLocalizedMessage().equals("no results found for query")){
//                        changeActivity(CreateHome.class);
//                    }
//                    else{
//                    }
//                }
//            }
//        });

    }

    /*
    * Method allows user to enter an Id of a Home,
    * then queries through homes to find Home with matching homeId not objectId
    * home id is public objectId is private.
    * */
    public void searchHome(View view){
        TextView resultsView = findViewById(R.id.searchResults);
        EditText searchExistingHomeEditText = findViewById(R.id.searchExistingHomeEditText);
        logToast("Homes.java -> searchHome() entered into editText: " , searchExistingHomeEditText.getText().toString() +"------------");
        ParseQuery<ParseObject> idQuery = ParseQuery.getQuery("Homes");
        idQuery.whereEqualTo("ID",searchExistingHomeEditText.getText().toString());
        idQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    String foundHomeName = "";

                    if(objects.size() > 0){
                        for (ParseObject object : objects) {
                            if(object.getString("ID").equals(searchExistingHomeEditText.getText().toString())){
                                logToast("Homes.java -> searchHome(): objectID : editText value ->" , object.getString("ID") +":" + searchExistingHomeEditText.getText().toString() + " ------------");
                            }
                        }
                        foundHomeObject = objects.get(0);
                        foundHomeName += objects.get(0).getString("HomeName") + "\n";
                        logToast("Homes.java -> searchHome(): objectId at index 0 : editText value ->" , foundHomeObject.getString("ID") +":" + searchExistingHomeEditText.getText().toString() + " ------------");

                    }
                    else{
                        logToast("ERROR Homes.java -> searchHome(): " , e.getLocalizedMessage() +"------------");
                    }

                    resultsView.setText(foundHomeName);
                    resultsView.setVisibility(View.VISIBLE);
                }
                else{
                }
            }
        });

    }

    /*
    * After a user searches a home the user can click on it to view information about the home
    * and in the future the user will be able to send a join request to the members of the home.
    *
    * Method Creates an Intent to Switch to ViewHome activity and also send's the HomeId along
    * with the Intent.
    * */
    public void viewHome(View view){
        EditText searchExistingHomeEditText = findViewById(R.id.searchExistingHomeEditText);
        Intent viewHome = new Intent(getApplicationContext(), ViewHome.class);
        viewHome.putExtra("HomeId", searchExistingHomeEditText.getText().toString());
        startActivity(viewHome);
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
    //Helper method to change activites quickly
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }

    //helper method show a user's homes when multi home feature is implemented
    public void showUsersHomes(View view){
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // proceed with the action (setting exact alarms)
                //alarmManager.setExact(...)
                //("Thanks:","Thank you for allowing healthyhome to schedule alarms for you");
            }
            else {
                logToast("IMPORTANT:","HealthyHome will not work properly if it cannot schedule alarms for you");
                // permission not yet approved. Display user notice and gracefully degrade
                //your app experience.
                        //alarmManager.setWindow(...)
            }
        }
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.SCHEDULE_EXACT_ALARM }, REQUEST_CODE);
//            logToast("IMPORTANT:","HealthyHome will not work properly if it cannot schedule alarms for you");
//        }
        changeActivity(MyHomes.class);
    }
//    public void onRequestPermissionsResult(int requestCode,  @NonNull int[] grantResults) {
//        if (requestCode == REQUEST_CODE) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                logToast("Thanks:","Thank you for allowing healthyhome to schedule alarms for you");
//            } else {
//                logToast("IMPORTANT:","HealthyHome will not work properly if it cannot schedule alarms for you");
//            }
//        }
//    }
    protected void onResume() {
        super.onResume();
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                // proceed with the action (setting exact alarms)
                //alarmManager.setExact(...)
                logToast("onResume()","---------------------");
            }
            else {
                logToast("IMPORTANT:","HealthyHome will not work properly if it cannot schedule alarms for you");
                // permission not yet approved. Display user notice and gracefully degrade
                //your app experience.
                //alarmManager.setWindow(...)
            }
        }

//        setBottomNavListener();
//        retrieveCurrentFCMRegistrationToken();
//        user = ParseUser.getCurrentUser();


    }


    //A token produced by FCM to identify the device not the user
    //used to send a notification from console to a specific device.
    public void retrieveCurrentFCMRegistrationToken(){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = token.toString();
                        sendTokenToServer(token);
                    }
                });
    }
    //Sending the token to server each time the Homes activity starts in the onCreate()
    public void sendTokenToServer(String token){
        ParseUser user = ParseUser.getCurrentUser();
        user.put("FCMDeviceToken",token);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                }
                else{
                }
            }
        });
    }
    public void logToast(String tag, String text) {
        Log.d(tag, text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }
}