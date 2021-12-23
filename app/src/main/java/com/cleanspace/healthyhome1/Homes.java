package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
//                    Log.i("TEST","TEST");
//                    Toast.makeText(getApplicationContext(),"User is only allowed one home temporarily", Toast.LENGTH_SHORT).show();
//                }
//                else{
//                    if(e.getLocalizedMessage().equals("no results found for query")){
//                        changeActivity(CreateHome.class);
//                    }
//                    else{
//                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
//                        Log.i("Exeption thrown", e.getLocalizedMessage());
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

        ParseQuery<ParseObject> idQuery = ParseQuery.getQuery("Homes");
        idQuery.whereEqualTo("ID",searchExistingHomeEditText.getText().toString());
        idQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    String foundHomeName = "";

                    if(objects.size() > 0){
                        foundHomeObject = objects.get(0);
                        foundHomeName += objects.get(0).getString("HomeName") + "\n";
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"No home found by that ID", Toast.LENGTH_SHORT).show();
                    }

                    resultsView.setText(foundHomeName);
                    resultsView.setVisibility(View.VISIBLE);
                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
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
        //TODO: go to "ViewHome" activity to view home details and send join request.
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
                                Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_SHORT).show();
                                changeActivity(MainActivity.class);
                            }else{
                                user.put("isLoggedIn",true);
                                user.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
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
        changeActivity(MyHomes.class);
    }

    //A token produced by FCM to identify the device not the user
    //used to send a notification from console to a specific device.
    public void retrieveCurrentFCMRegistrationToken(){

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN ", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        String msg = token.toString();
                        Log.d("TOKEN ", msg);
                        //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
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
                    Log.i("Token","New token generated and sent to parse server");
                    //Toast.makeText(getApplicationContext(),"New FCM device token generated",Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.i("Token",e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}