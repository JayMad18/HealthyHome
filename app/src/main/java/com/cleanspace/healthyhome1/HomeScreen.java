package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;

public class HomeScreen extends AppCompatActivity {
    TextView quoteView;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        generateQuote();
        setLogoutListener();
    }
    public void setLogoutListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.logoutItem){
                   logoutAlertDialog();
                }
                return false;
            }
        });
    }
    public void logoutAlertDialog(){
        new AlertDialog.Builder(this).setTitle("Log out").setMessage("Are you sure you want to log out?")
                .setIcon(android.R.drawable.ic_media_previous).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                logout();
            }
        }).setNegativeButton("No", null).show();
    }
    public void logout(){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(getApplicationContext(),"Logged Out", Toast.LENGTH_SHORT).show();
                    changeActivity(MainActivity.class);
                }else{
                    Log.i("ERROR!!!!!!", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }
    @SuppressLint("SetTextI18n")
    private void generateQuote(){
        quoteView = findViewById(R.id.quoteView);
        DownloadRandomQuote randomQuote = new DownloadRandomQuote();
        try{
            JSONObject jsonData = randomQuote.execute("https://api.quotable.io/random?maxLength=50").get();
            Log.i("JSON data tostring", jsonData.toString());
            String content = jsonData.getString("content");
            String author = jsonData.getString("author");
            quoteView.setText(content + " "+author);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

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
            //e.printStackTrace();
            Log.i("Failed at do in background", e.toString());
        }

        return null;
    }
}