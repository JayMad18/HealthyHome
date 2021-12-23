package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class UserInfo extends AppCompatActivity {
    TextView nameTextView, userNameTextView, emailTextView;
    BottomNavigationView bottomNavigationView;

    String selectedHomeObjectId;
    String homeName;

    /*
    * Only onCreate method that displays info on the clicked user as soon as activity created.
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        Intent userInfo = getIntent();

        nameTextView = findViewById(R.id.nameTextView);
        userNameTextView = findViewById(R.id.userNameTextView);
        emailTextView = findViewById(R.id.emailTextView);

        homeName = userInfo.getStringExtra("HomeName");
        selectedHomeObjectId = userInfo.getStringExtra("HomeObjectID");// <---| this variable is put before setBottomNavListener incase b/c I believe
        setBottomNavListener();                                                // <---| an error ma be thrown if the user tries to go back before
                                                                               //       selectedHomeObjectId has been initailized.
        nameTextView.setText("Name: "+userInfo.getStringExtra("name"));
        userNameTextView.setText("Username: "+userInfo.getStringExtra("username"));
        emailTextView.setText("Email: "+userInfo.getStringExtra("email"));
    }
    public void setBottomNavListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.backItem){
                    changeActivity(ShowMembers.class);
                }
                return false;
            }
        });
    }
    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeObjectID", selectedHomeObjectId);
        switchActivity.putExtra("HomeName", homeName);
        startActivity(switchActivity);
    }
}