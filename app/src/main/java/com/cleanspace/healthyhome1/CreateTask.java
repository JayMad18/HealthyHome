package com.cleanspace.healthyhome1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.KeyboardShortcutGroup;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.LogOutCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTask extends AppCompatActivity {
    EditText taskNameEditText, detailsEditText;
    TextView taskNameTextView, detailsTextView;
    BottomNavigationView bottomNavigationView;
    Button createTaskButton;

    ListView membersListView;



    String selectedHomeObjectId;
    String assignerObjectId;
    String TOPIC;

    boolean isAssigned, taskHasObjectId;

    int position;


    ParseObject homeObject;
    ParseObject task;
    ParseUser user;
    ParseObject assignedMember;


    ArrayList<String> registrationTokens = new ArrayList<String>();
    ArrayList<ParseUser> memberObjects = new ArrayList<ParseUser>();
    ArrayList<String> memberNames = new ArrayList<String>();
    ArrayList<String> memberObjectIds = new ArrayList<String>();


    /**
     * TODO fix issue when user wants to immediatly create another task after another
     *         issue: previously created task gets overwritten if user never reloads the activity.
     *
     *         Solution: switching back to HomeScreen after task is created.
     *
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        taskNameEditText = findViewById(R.id.taskNameEditText);
        detailsEditText = findViewById(R.id.detailsEditText);

        taskNameTextView = findViewById(R.id.taskNameTextView);
        detailsTextView = findViewById(R.id.detailsTextView);

        createTaskButton = findViewById(R.id.createTaskButton);

        membersListView = findViewById(R.id.membersListView);

        user = ParseUser.getCurrentUser();




        Intent homeObjectId = getIntent();
        selectedHomeObjectId = homeObjectId.getStringExtra("HomeObjectID");
        TOPIC = "/topics/"+selectedHomeObjectId + "TOPIC";

        //getting homeObject just incase
        ParseQuery<ParseObject> homeQuery = ParseQuery.getQuery("Homes");
        homeQuery.getInBackground(selectedHomeObjectId, new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if(e == null){
                    homeObject = object;
                    task = new ParseObject("Tasks");
                }
                else {
                }
            }
        });
        setBottomNavListener();
        populateListView();
        keyboardDetector();
        assignToMemberItemClickListener();

    }
    /*
    * Creates and saves task to the server.
    *
    * */
    /*TODO:
    * -Assign task to clicked member from listview
    * -radio button to select active/not active
    * */
    public void createTask(View view){

        String assignedMemberObjectId = memberObjects.get(position).getObjectId();

        task.put("Name", taskNameEditText.getText().toString());
        task.put("Home", selectedHomeObjectId);
        task.put("details",detailsEditText.getText().toString());

        if(isAssigned){
            task.put("isAssigned", true);
            task.put("assignToObjectId", assignedMemberObjectId);
            task.put("assignerObjectId",assignerObjectId);
        }
        else{
            task.put("isAssigned", false);
        }

        task.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    if(isAssigned){
                        taskHasObjectId = true;
                        buildJSONMessageObject(isAssigned);
                        changeActivity(HomeScreen.class);

                    }
                    else{
                        buildJSONMessageObject(isAssigned);
                        changeActivity(HomeScreen.class);

                    }
                }
                else{
                }
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
                        memberObjects.add(user);
                    }
                    memberNamesAdapter.notifyDataSetChanged();
                    membersListView.setAdapter(memberNamesAdapter);
                }else{
                }
            }
        });
    }
    public void assignToMemberItemClickListener(){
        membersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(taskNameEditText.getText().length() == 0){
                }
                else{
                    confirmAssignTaskToMember(position);
                }
            }
        });
    }
    public void confirmAssignTaskToMember(int position){
        this.position = position;
        new AlertDialog.Builder(this).setTitle("Assign Member").setMessage("Are you sure you want to assign " + memberObjects.get(position).get("name")+" to this task?")
                .setPositiveButton("Assign", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                                isAssigned = true;
                                assignedMember = memberObjects.get(position);
                                assignerObjectId = ParseUser.getCurrentUser().getObjectId().toString();

                    }
                }).setNegativeButton("Don't assign", null).show();
    }

    //bottom nav item click listener
    public void setBottomNavListener(){
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if(item.getItemId() == R.id.backItem){
                    changeActivity(HomeScreen.class);
                }
                return false;
            }
        });
    }
    /*
     * A logout alert dialog that ask's if sure want to log out,
     * this needs to be implemented in all activites that allow user to log out
     * */
//    public void logoutAlertDialog(){
//        new AlertDialog.Builder(this).setTitle("Log out").setMessage("Are you sure you want to log out?")
//                .setIcon(android.R.drawable.ic_media_previous).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                logout();
//            }
//        }).setNegativeButton("No", null).show();
//    }
//    //logout in its own method
//    public void logout(){
//        user.put("isLoggedIn",false);
//        user.saveInBackground(new SaveCallback() {
//            @Override
//            public void done(ParseException e) {
//                if(e == null){
//                    ParseUser.logOutInBackground(new LogOutCallback() {
//                        @Override
//                        public void done(ParseException e) {
//                            if(e == null){
//                                changeActivity(MainActivity.class);
//                            }else{
//                                user.put("isLoggedIn",true);
//                                user.saveInBackground(new SaveCallback() {
//                                    @Override
//                                    public void done(ParseException e) {
//                                    }
//                                });
//                            }
//                        }
//                    });
//                }
//            }
//        });
//    }
    //helper method to quickly switch activites
    public void changeActivity(Class activity){
        Intent switchActivity = new Intent(getApplicationContext(), activity);
        switchActivity.putExtra("HomeObjectID", selectedHomeObjectId);
        startActivity(switchActivity);
    }

//    public void loadRegistrationTokens(){
//        /*
//         * Queries all users that are logged in and the loops through each user to see of there homeList contains this home.
//         * */
//        ParseQuery<ParseUser> inThisHomeQuery = ParseUser.getQuery();
//        inThisHomeQuery.whereEqualTo("isLoggedIn", true);
//        inThisHomeQuery.findInBackground(new FindCallback<ParseUser>() {
//            @Override
//            public void done(List<ParseUser> objects, ParseException e) {
//                if(e == null){
//                    if(objects.size() > 0){
//                        for(ParseUser member : objects){
//                            ArrayList<String> homeList = (ArrayList<String>) member.get("HomeList");
//                            if(homeList.contains(selectedHomeObjectId)){
//                                registrationTokens.add((String) member.get("FCMDeviceToken"));
//                            }
//                        }
//                        buildJSONMessageObject(isAssigned);
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
    public void buildJSONMessageObject(boolean isAssigned){
        JSONObject notification = new JSONObject();
        JSONObject notificationBody = new JSONObject();
        if(isAssigned == true){
            try {
                notificationBody.put("title", "New Task");
                notificationBody.put("message", ParseUser.getCurrentUser().getUsername()+" has assigned " +
                        assignedMember.get("name") + " to " + task.get("Name"));

                notification.put("to", TOPIC);
                //notification.put("registration_ids",registrationTokens);
                notification.put("data", notificationBody);
            } catch ( JSONException e) {
            }
        }
        else{
            try {
                notificationBody.put("title", "New Task");
                notificationBody.put("message", ParseUser.getCurrentUser().getUsername()+" has created a new task: " + task.get("Name"));

                notification.put("to", TOPIC);
                //notification.put("registration_ids",registrationTokens);
                notification.put("data", notificationBody);
            } catch ( JSONException e) {
            }
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





    /**
     * I really dont know how these two methods below achieve my desire to detect when the keyboard
     * is open,"In a step by step sense", but they both work together to detect when the keyboard is open.
     *
     * Just copy both, including "boolean isKeyboardShowing = false;"
     * Paste somewhere in code,
     * Call keyboardDetector() in onCreate(),
     * modify keyboardDetector() as needed.
     *  -in this case I have modified keyBoardDetector() to hide all
     *  un-used UI elements while keyboard is open.
     */
    boolean isKeyboardShowing = false;
    void onKeyboardVisibilityChanged(boolean opened) {
//        if(opened == false){
//        }
//        else{
//        }

    }
    public void keyboardDetector(){
        createTaskButton.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {

                        Rect r = new Rect();
                        createTaskButton.getWindowVisibleDisplayFrame(r);
                        int screenHeight = createTaskButton.getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;

                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            if (!isKeyboardShowing) {
                                isKeyboardShowing = true;
                                onKeyboardVisibilityChanged(true);
                                taskNameTextView.setVisibility(View.GONE);
                                detailsTextView.setVisibility(View.GONE);
                                createTaskButton.setVisibility(View.GONE);
                                membersListView.setVisibility(View.GONE);
                            }
                        }
                        else {
                            // keyboard is closed
                            if (isKeyboardShowing) {
                                isKeyboardShowing = false;
                                onKeyboardVisibilityChanged(false);
                                taskNameTextView.setVisibility(View.VISIBLE);
                                detailsTextView.setVisibility(View.VISIBLE);
                                createTaskButton.setVisibility(View.VISIBLE);
                                membersListView.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}