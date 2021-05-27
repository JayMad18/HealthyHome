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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ShowMembers extends AppCompatActivity {
    ArrayList<String> memberNames = new ArrayList<String>();
    ArrayList<String> memberObjectIds = new ArrayList<String>();
    ArrayList<ParseUser> parseUsers = new ArrayList<ParseUser>();
    ListView membersListView;
    TextView titleView;
    String selectedHomeObjectId;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_members);

        Intent homeObjectId = getIntent();
        selectedHomeObjectId = homeObjectId.getStringExtra("HomeObjectID");

        membersListView = findViewById(R.id.membersListView);
        titleView = findViewById(R.id.title);
        titleView.setText(homeObjectId.getStringExtra("HomeName") + " members");
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        populateListView();
        setLogoutListener();
        onItemClickListener();


    }
    public void onItemClickListener(){
      membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              Intent sendUserInfo = new Intent(getApplicationContext(), UserInfo.class);
              //TODO: start new activity showing clicked user info.
              ParseUser selectedUser = parseUsers.get(position);
              sendUserInfo.putExtra("objectId", selectedUser.getObjectId());
              sendUserInfo.putExtra("name", selectedUser.get("name").toString());
              sendUserInfo.putExtra("username",selectedUser.getUsername());
              //selected user needs a session token to use .getEmail(), which means only logged in user can use .getEmail()
              sendUserInfo.putExtra("email",selectedUser.get("EMAIL").toString());
              startActivity(sendUserInfo);
          }
      });
    }

    public void populateListView(){
        ArrayAdapter<String> memberNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,memberNames);

        ParseQuery<ParseUser> memberQuery = ParseUser.getQuery();
        memberQuery.whereContains("HomeList", selectedHomeObjectId);
        memberQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    for(ParseUser user: objects){
                        memberNames.add(user.get("name").toString());
                        memberObjectIds.add(user.getObjectId());
                        parseUsers.add(user);
                    }
                    memberNamesAdapter.notifyDataSetChanged();
                    membersListView.setAdapter(memberNamesAdapter);
                }else{
                    Toast.makeText(getApplicationContext(),"Error loading members "+e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                    Log.d("error loading homes",e.getLocalizedMessage());
                }
            }
        });
    }
    public void setLogoutListener(){

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.logoutItem){
                    logoutAlertDialog();
                }
                else if(item.getItemId() == R.id.backItem){
                    changeActivity(Homes.class);
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
}