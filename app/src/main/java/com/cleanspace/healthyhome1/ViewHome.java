package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import com.google.firebase.messaging.RemoteMessage;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseSession;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.firebase.messaging.Constants.MessagePayloadKeys.SENDER_ID;

//Hello development branch!
//Hello im development branch and I come from the remote repository!
public class ViewHome extends AppCompatActivity {
    ParseObject foundHomeObject;

    ArrayList<String> memberNames = new ArrayList<String>();
    ArrayList<String> registrationTokens = new ArrayList<String>();
    ArrayAdapter<String> memberNamesAdapter;

    TextView homeNameView, homeIdView, numberOfMembersView, numberOfTasksView;
    ListView membersListView;

    BottomNavigationView bottomNavigationView;

    String TOPIC;

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
                    ParsePush.subscribeInBackground(foundHomeObject.getObjectId()+ "Channel");


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
                            else if(item.getItemId() == R.id.joinItem){

                                //TODO:Send Join request to members of the retrieved home to be accepted or rejected. Via Push Notification.
                                addMemberToHome();
                            }
                            return false;
                        }
                    });

                }
                else{
                    Toast.makeText(getApplicationContext(),e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
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
                        saveHomeToArrayList(user, foundHomeObject);

                    }
                }
            });
        }
        else{//self explanitory
            Toast.makeText(getApplicationContext(),"User already belongs to this home", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                else{
                    subscribeToHomeFCMTopic();
                    populateListView();
                }
            }
        });
    }

    public void subscribeToHomeFCMTopic(){
        FirebaseMessaging.getInstance().subscribeToTopic("/topics/"+foundHomeObject.getObjectId() + "TOPIC")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribe to home success";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe to home failed";
                        }
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                        TOPIC = "/topics/"+foundHomeObject.getObjectId() + "TOPIC";
                        buildJSONMessageObject();
                        //loadRegistrationTokens();
                    }
                });
    }

//    public void loadRegistrationTokens(){
//        /*
//        * Queries all users that are logged in and the loops through each user to see of there homeList contains this home.
//        * */
//        ParseQuery<ParseUser> inThisHomeQuery = ParseUser.getQuery();
//        inThisHomeQuery.whereEqualTo("isLoggedIn", true);
//        inThisHomeQuery.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if(e == null){
//                    Log.i("query session size", Integer.toString(objects.size()));
//                    if(objects.size() > 0){
//                        for(ParseUser member : objects){
//                            ArrayList<String> homeList = (ArrayList<String>) member.get("HomeList");
//                            if(homeList.contains(foundHomeObject.getObjectId())){
//                                registrationTokens.add((String) member.get("FCMDeviceToken"));
//                            }
//                        }
//                        buildJSONMessageObject();
//                    }
//                }
//            }
//        });
//    }

    /*
    *create a JsonObject of the notification body
    * This object will contain the receiver’s topic, notification title,
    * notification message, and other key/value pairs to add.
    * */
    public void buildJSONMessageObject(){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        try {
            notificationBody.put("title", "New Member");
            notificationBody.put("message", ParseUser.getCurrentUser().getUsername()+" has joined " + foundHomeObject.get("HomeName"));

            notification.put("to", TOPIC);
            //notification.put("registration_ids",registrationTokens);
            notification.put("data", notificationBody);
        } catch ( JSONException e) {
        }

        sendNotification(notification);
    }

    /*
    *make a network request to FCM server using Volley library,
    *then the server will use the request parameters to route the
    *notification to the targeted device.
    * */
    public void sendNotification(JSONObject notification){
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(getString(R.string.FCM_API), notification,
                new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "Request error", Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization","key="+getString(R.string.server_key));
                params.put("Content-Type", getString(R.string.content_type));
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
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

        memberNamesAdapter = new ArrayAdapter<String>(this, R.layout.list_layout, R.id.list_content,memberNames);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereContains("HomeList", foundHomeObject.getObjectId());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    for(ParseUser object: objects){
                        if(!memberNames.contains(object.getUsername())){
                            memberNames.add(object.getUsername());
                        }
                    }
                    memberNamesAdapter.notifyDataSetChanged();
                    membersListView.setAdapter(memberNamesAdapter);
                    numberOfMembersView.setText("Number of members: " + Integer.toString(memberNames.size()));
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