package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class UserInfo extends AppCompatActivity {
    TextView nameTextView, userNameTextView, emailTextView;
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

        nameTextView.setText("Name: "+userInfo.getStringExtra("name"));
        userNameTextView.setText("Username: "+userInfo.getStringExtra("username"));
        emailTextView.setText("Email: "+userInfo.getStringExtra("email"));
        Log.i("Caught email", emailTextView.getText().toString());
    }
}