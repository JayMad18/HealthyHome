package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
//Hello development branch!
//Hello im development branch and I come from the remote repository!
public class ViewHome extends AppCompatActivity {
    ParseObject foundHomeObject;
    ArrayList<String> memberNames = new ArrayList<String>();
    TextView homeNameView, homeIdView, numberOfMembersView, numberOfTasksView;
    BottomNavigationView bottomNavigationView;

    /*
    * onCreate() method recieves intent and data sent with intent after creating activity
    *
    * also initializes bottemNav view
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_home);

        Intent sentHomeId = getIntent();

        String homeId = sentHomeId.getStringExtra("HomeId");

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getHome(homeId);

    }


    /*
    * This method uses the homeId sent with the Intent in a query to find the particular Home's details
    * once home is found it calls the populateMembersAndTaskViews(String) method
    * then sets LogoutListner in the bottomNav
    * */
    public void getHome(String homeId){
         homeNameView = findViewById(R.id.homeNameTextView);
         homeIdView = findViewById(R.id.homeIdTextView);

        //Getting the home by querying Id
        //Replace with homeIdQuery.getFirstInBackground(new GetCallBack<ParseObject>() {...});
        ParseQuery<ParseObject> homeIdQuery = ParseQuery.getQuery("Homes");
        homeIdQuery.whereEqualTo("ID", homeId);
        homeIdQuery.include("MembersList");
        homeIdQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){

                    Toast.makeText(getApplicationContext(),"Home found", Toast.LENGTH_SHORT).show();

                    //Getting the first home object found until uniqueID is solved.

                    foundHomeObject = objects.get(0);

                    homeNameView.setText("Home Name: " + foundHomeObject.get("HomeName").toString());
                    homeIdView.setText("Home Id: " + foundHomeObject.get("ID").toString());


                    populateMemberAndTaskViews(foundHomeObject.get("ID").toString());
                    /*
                    * Bottom navigation item selection listener method from the bottomNav View class
                    * */
                    bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            //only item that has a name id, any other item clicked calls the else statement
                            if(item.getItemId() == R.id.backItem){
                                changeActivity(Homes.class);
                            }
                            else{

                                //TODO:Send Join request to members of the retrieved home to be accepted or rejected. Via Push Notification.
                                addMemberToHome();
                            }
                            return false;
                        }
                    });

                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    Log.i("Error searching home ID", e.getLocalizedMessage());
                }
            }
        });
    }

    /*
    * Called by the bottomNav listener method whenever the backItem is NOT clicked.
    * adds the current session user to the home
    * in the future it will first send a join request push notification to current members of the home
    * to be accepted or rejected.
    * */
    public void addMemberToHome(){
        ParseUser user = ParseUser.getCurrentUser();

        //Home saves user to MembersList arraylist
        ArrayList<String> membersList = (ArrayList) foundHomeObject.getList("MembersList");
        ArrayList<String> homesList = (ArrayList) user.getList("HomeList");
        if(!membersList.contains(user.getObjectId()) && !homesList.contains(foundHomeObject.getObjectId())){
            membersList.add(user.getObjectId());
            foundHomeObject.put("MembersList", membersList);
            foundHomeObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null){
                        Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), user.getObjectId(),Toast.LENGTH_SHORT).show();
                        Log.i("User Object ID saved", user.getObjectId());
                        saveHomeToArrayList(user, foundHomeObject);
                        populateListView();
                    }
                }
            });
        }
        else{//self explanitory
            Toast.makeText(getApplicationContext(),"User already belongs to this home", Toast.LENGTH_SHORT).show();
            Log.i("User Object ID rejected", user.getObjectId());
        }

    }

    /*
    * Called when the user joins the home
    * this method adds this home to the user's list of homes that he/she is currently apart of.
    * */
    public void saveHomeToArrayList(ParseUser user, ParseObject home){
        ArrayList<String> homesList = (ArrayList) user.getList("HomeList");
        homesList.add(home.getObjectId());
        user.put("HomeList",homesList);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    Log.i("Error saving homesList to HomeList column", e.getLocalizedMessage());
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
    * This method name is partially inaccurate as it only populates the numberOfTasksView with the number of current tasks the home has.
    *
    * calls populateListView which fills a listview with names of members of the home.
    *
    * gets number of tasks by searching for tasks that contain the current Home objectId in Home column
    * then gets the number of tasks from the size of the returned objects list from findInBackGround(){..} method.
    * */
    public void populateMemberAndTaskViews(String homeId){
        ArrayList<String> membersList = (ArrayList) foundHomeObject.getList("MembersList");
        numberOfMembersView = findViewById(R.id.numberOfMembersTextView);

        populateListView();

        //getting number of tasks from task pointer
        ParseQuery<ParseObject> taskQuery = ParseQuery.getQuery("Homes");
        taskQuery.whereEqualTo("Home", foundHomeObject.getObjectId());
        taskQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    numberOfTasksView = findViewById(R.id.numberOfTasksTextView);
                    numberOfTasksView.setText("Number of tasks created: " + Integer.toString(objects.size()));
                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                    Log.i("Error getting task count", e.getLocalizedMessage());
                }
            }
        });
    }

    /*
    *Populates the listview in the middle of the screen with the names of the members
    * who currently reside in that home.
    * Also updates the number of members textview
    * */
    public void populateListView(){
        ListView membersListView = findViewById(R.id.membersListView);

        ArrayAdapter<String> memberNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,memberNames);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("HomeList", foundHomeObject.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                Log.i("Done",Integer.toString(objects.size())+" objects found in query");
                if(e == null){
                    for(ParseUser object: objects){
                        if(!memberNames.contains(object.getUsername())){
                            memberNames.add(object.getUsername());
                        }
                    }
                    memberNamesAdapter.notifyDataSetChanged();
                    membersListView.setAdapter(memberNamesAdapter);
                    numberOfMembersView.setText("Number of members: " + Integer.toString(memberNames.size()));
                }else{
                    Log.i("Error updating listview", e.getLocalizedMessage());
                }
            }
        });
        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO this is where the code will go that will display the members info when clivked on.
                Toast.makeText(getApplicationContext(), memberNames.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        startActivity(switchActivity);
    }




}