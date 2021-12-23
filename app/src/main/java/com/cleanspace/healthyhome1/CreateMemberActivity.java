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
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.ArrayList;
import java.util.List;

public class CreateMemberActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_member);

    }

    /*
    * Calls two methods helper methods to make sure all textfields are filled
    * and both passwords match before Saving the user to the server.
    *
    * -note: any Array columns that exist on the server need to be defined here and saved
    * so that the Array column for that user is not undifined.
    * */
    public void submit(View view){
        /* TODO submit member profile data to parse server and Log Into Home Dashboard */
       if(allTextFieldsFilled()){
           if(passwordMatch()){
               EditText name = findViewById(R.id.nameEditText);
               EditText username = findViewById(R.id.userNameEditText);
               EditText email = findViewById(R.id.emailEditText);
               EditText password = findViewById(R.id.passwordEditText);

               ParseUser user = new ParseUser();

               ArrayList<String> homesList = new ArrayList<String>();
               ArrayList<String> taskList = new ArrayList<String>();
               user.put("taskList",taskList);
               user.put("HomeList", homesList);
               user.put("name", name.getText().toString());
               user.put("EMAIL",email.getText().toString());

               user.setUsername(username.getText().toString());
               user.setPassword(password.getText().toString());
               user.setEmail(email.getText().toString());

               //ParseUser class method to sign up
               user.signUpInBackground(new SignUpCallback() {
                   public void done(ParseException e) {
                       if (e == null) {user.put("isLoggedIn", true);
                           user.saveInBackground(new SaveCallback() {
                               @Override
                               public void done(ParseException e) {
                                   if(e == null){

                                       /*
                                        *  -we dont have to send any extra data containing info to identify the current user since
                                        *   now that the user is created and logged in, we can use the ParseUser.getCurrentUser() method to get the current user.
                                        * */
                                       changeActivity(Homes.class);
                                   }
                               }
                           });


                       } else {
                           // Sign up didn't succeed. Look at the ParseException
                           // to figure out what went wrong
                       }
                   }
               });
           }
           else{
           }
       }
       else{
       }
    }
    //Switches to the MainActivty acticity
    public void goBack(View view){
        changeActivity(MainActivity.class);
    }
    /*
    * Calls the ParseUser class method "LogOutInBackground(LogOutCallback(){...})" to log the user out
    * */
    public void logout(View view){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Button logoutButton = findViewById(R.id.logoutButton);
                    logoutButton.setVisibility(View.GONE);
                } else {
                    // Sign up didn't succeed. Look at the ParseException
                    // to figure out what went wrong
                }
            }
        });

    }

    //Checks if all textfields have been filled out before allowing submit, returns boolean
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
    //Checks if both passwords match before allowing submit, returns boolean
    public Boolean passwordMatch(){
        EditText password = findViewById(R.id.passwordEditText);
        EditText conPassword = findViewById(R.id.confirmPasswordEditText);

        if(password.getText().toString().matches(conPassword.getText().toString())){
            return true;
        }
        return false;
    }
    //helper method to return a boolean if a textfield is empty
    private boolean isEmpty(EditText etText) {
        if (etText.getText().toString().trim().length() > 0) {
            return false;
        }
        return true;
    }
    /*
     * This method onResume() is used in case the user somehow makes their way to
     * the CreateMemberActivity activity while already logged in i.e., there is already a session token active.
     * The method will automatically log out the current user to prevent session token glitches
     * */
    @Override
    protected void onResume() {
        super.onResume();
        if(ParseUser.getCurrentSessionToken() != null){
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){
                    }
                    else {
                    }
                }
            });
        }
    }
    //helper method to quickly switch activites without sending any Extra's
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
}