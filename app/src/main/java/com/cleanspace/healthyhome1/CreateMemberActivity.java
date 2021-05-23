package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class CreateMemberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_member);

    }

    public void submit(View view){
        /* TODO submit member profile data to parse server and Log Into Home Dashboard */
       if(allTextFieldsFilled()){
           if(passwordMatch()){
               EditText name = findViewById(R.id.nameEditText);
               EditText username = findViewById(R.id.userNameEditText);
               EditText email = findViewById(R.id.emailEditText);
               EditText password = findViewById(R.id.passwordEditText);
               EditText conPassword = findViewById(R.id.confirmPasswordEditText);

               ParseUser user = new ParseUser();

               ArrayList<String> homesList = new ArrayList<String>();
               user.put("HomeList", homesList);
               user.put("name", name.getText().toString());

               user.setUsername(username.getText().toString());
               user.setPassword(password.getText().toString());
               user.setEmail(email.getText().toString());

               user.signUpInBackground(new SignUpCallback() {
                   public void done(ParseException e) {
                       if (e == null) {
                           Log.i("Session Token", user.getSessionToken());
                           Toast.makeText(getApplicationContext(),"Submitted Succefully", Toast.LENGTH_LONG).show();
                           changeActivity(Homes.class);
                       } else {
                           // Sign up didn't succeed. Look at the ParseException
                           // to figure out what went wrong
                           Log.i("Error!!!!!!", e.getLocalizedMessage());
                           Toast.makeText(getApplicationContext(),"Error: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                       }
                   }
               });
           }
           else{
               Toast.makeText(getApplicationContext(),"Passwords do not match!", Toast.LENGTH_LONG).show();
           }
       }
       else{
           Toast.makeText(getApplicationContext(),"Please fill out all text fields", Toast.LENGTH_LONG).show();
       }
    }
    public void goBack(View view){
        changeActivity(MainActivity.class);
    }
    public void logout(View view){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Log.i("Logged out", "BYE!");
                    Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_LONG).show();
                    Button logoutButton = findViewById(R.id.logoutButton);
                    logoutButton.setVisibility(View.GONE);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                    Log.i("Error logging out", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),"ERROR: error logging out", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    public Boolean allTextFieldsFilled(){
        EditText name = findViewById(R.id.nameEditText);
        EditText username = findViewById(R.id.userNameEditText);
        EditText email = findViewById(R.id.emailEditText);
        EditText password = findViewById(R.id.passwordEditText);
        EditText conPassword = findViewById(R.id.confirmPasswordEditText);

        if(isEmpty(name) || isEmpty(username) || isEmpty(email) || isEmpty(password) || isEmpty(conPassword)){
            return false;
        }
        return true;
    }
    public Boolean passwordMatch(){
        EditText password = findViewById(R.id.passwordEditText);
        EditText conPassword = findViewById(R.id.confirmPasswordEditText);

        if(password.getText().toString().matches(conPassword.getText().toString())){
            return true;
        }
        return false;
    }
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(ParseUser.getCurrentSessionToken() != null){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                        Log.i("Logged out succesfully", "");
                        Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Log.i("ERROR!!!", e.getLocalizedMessage());
                        Toast.makeText(getApplicationContext(),"Error logging out: "+ e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
}