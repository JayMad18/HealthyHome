package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1234;

    /*
    onCreate method first checks if the app already has a session token
    if session token exists then it automatically switches to the Homes activity
    else then it calls the usual "setContentView(R.layout.activity_main);" method
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Offload all processes from activities on to worker threads to increase performance

        if(ParseUser.getCurrentSessionToken() != null){
            changeActivity(Homes.class);
        }else {
            setContentView(R.layout.activity_main);

        }


    }
    //switches to CreateMemberActivity activity
    public void createMember(View view){
        changeActivity(CreateMemberActivity.class);
    }
    //switches to LoginActivity activity
    public void logIn(View view){
    changeActivity(LoginActivity.class);
    }

    /*Handle the user's response to the permission request by overriding the onRequestPermissionsResult() method in your activity:*/
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logToast("Thanks:","Thank you for allowing healthyhome to schedule alarms for you");
            } else {
                logToast("IMPORTANT:","HealthyHome will not work properly if it cannot schedule alarms for you");
            }
        }
    }
    /*
    helper method to quckly call an Intent to switch classes,
    usually used when no Extra's need to be sent
    * */
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
    public void logToast(String tag, String text){
        Log.d(tag,text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }

}