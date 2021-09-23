package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.ParseObject;
import com.parse.ParseUser;

public class MainActivity extends AppCompatActivity {

    /*
    onCreate method first checks if the app already has a session token
    if session token exists then it automatically switches to the Homes activity
    else then it calls the usual "setContentView(R.layout.activity_main);" method
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    /*
    helper method to quckly call an Intent to switch classes,
    usually used when no Extra's need to be sent
    * */
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }

}