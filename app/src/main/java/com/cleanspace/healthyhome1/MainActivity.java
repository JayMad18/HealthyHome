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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(ParseUser.getCurrentSessionToken() != null){
            changeActivity(Homes.class);
        }else {
            setContentView(R.layout.activity_main);
        }

    }

    public void createMember(View view){
        changeActivity(CreateMemberActivity.class);
    }

    public void logIn(View view){
    changeActivity(LoginActivity.class);
    }

    public void goNavTest(View view){
        changeActivity(HomeScreen.class);
    }

    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }

}