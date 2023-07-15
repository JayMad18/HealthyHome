package com.cleanspace.healthyhome1;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AddUserToHomeWorker extends Worker {
    String foundHomeObjectID,requestedUserObjectID, topic, personalTopic;
    ParseUser user;
    ParseObject home;

    ArrayList<String> membersList = new ArrayList<String>();
    ArrayList<String> homesList = new ArrayList<String>();


    public AddUserToHomeWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        /*
        *
            notificationBody.put("foundHomeObjectID", foundHomeObjectID);
            notificationBody.put("requestedUserObjectID", requestedUserObjectID);
            notificationBody.put("topic", topic);
            notificationBody.put("personalTopic",personalTopic);
            notification.put("to", topic);
        * */

        try{
            foundHomeObjectID = getInputData().getString("foundHomeObjectID");
            logToast("AddUserToHomeWorker.java -> doWork(): ",  foundHomeObjectID+ "-----------------\n");
            requestedUserObjectID = getInputData().getString("requestedUserObjectID");
            logToast("AddUserToHomeWorker.java -> doWork(): ",  requestedUserObjectID+ "-----------------\n");
            topic = getInputData().getString("topic");
            logToast("AddUserToHomeWorker.java -> doWork(): ",  topic+ "-----------------\n");
            personalTopic = getInputData().getString("personalTopic");
            logToast("AddUserToHomeWorker.java -> doWork(): ",  personalTopic+ "-----------------\n");

            getUser();
        }catch(Exception e){
           logToast("Error AddUserToHomeWorker.java -> doWork(): ", e.getLocalizedMessage() + "-----------------");
        }

        return Result.success();
    }
    public void getUser(){
        logToast("AddUserToHomeWorker.java -> getUser(): ","called-----------------\n");
        ParseQuery<ParseUser> requestedUserQuery = ParseUser.getQuery();
        requestedUserQuery.whereEqualTo("objectId", requestedUserObjectID);
        requestedUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    user = objects.get(0);
                    logToast("AddUserToHomeWorker.java -> getUser(): ",  user.toString()+ "-----------------\n");
                    getHome();

                }
                else{
                    logToast("Error AddUserToHomeWorker.java -> getUser(): ", e.getLocalizedMessage() + "-----------------");
                }
            }
        });
    }
    public void getHome(){
        logToast("AddUserToHomeWorker.java -> getHome(): ","called-----------------\n");

        ParseQuery<ParseObject> requestedHomeQuery = ParseQuery.getQuery("Homes");
        requestedHomeQuery.whereEqualTo("objectId", foundHomeObjectID );
        requestedHomeQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    home = objects.get(0);
                    logToast("AddUserToHomeWorker.java -> getHome(): ",  home.toString()+ "-----------------\n");
                    addMemberToHome();


                }
                else{
                    logToast("Error AddUserToHomeWorker.java -> getHome(): ", e.getLocalizedMessage() + "-----------------");
                }
            }
        });

    }

    public void addMemberToHome(){
        logToast(" AddUserToHomeWorker.java -> addMemberToHome(): ",  "called!-----------------");
        //Home saves user to MembersList arraylist
        membersList.addAll(Objects.requireNonNull(home.getList("MembersList"))) ;
        ArrayList<String> tempHomeList = new ArrayList<String>();
        tempHomeList.addAll(Objects.requireNonNull(user.getList("HomeList")));
        logToast(" AddUserToHomeWorker.java -> addMemberToHome(): ",  "members and homes list filled-----------------");
        logToast(" AddUserToHomeWorker.java -> addMemberToHome():  home.getList(\"MembersList\"):",  home.getList("MembersList")+"-----------------");
        logToast(" AddUserToHomeWorker.java -> addMemberToHome(): user.getList(\"HomeList\"):",  user.getList("HomeList")+"-----------------");
        logToast(" AddUserToHomeWorker.java -> addMemberToHome():  membersList:",  membersList.toString()+"-----------------");
        logToast(" AddUserToHomeWorker.java -> addMemberToHome():  homesList:",  tempHomeList.toString()+"-----------------");
        if(!membersList.contains(user.getObjectId()) && !tempHomeList.contains(home.getObjectId())){
            logToast(" AddUserToHomeWorker.java -> addMemberToHome(): ",  "if statement called:true-----------------");
            membersList.add(user.getObjectId());
            logToast(" AddUserToHomeWorker.java -> addMemberToHome():  home.getList(\"MembersList\") after adding:",  membersList.toString()+"-----------------");
            home.put("MembersList", membersList);
            logToast(" AddUserToHomeWorker.java -> addMemberToHome():  home.put(\"MembersList\", membersList):",  home.getList("MembersList")+"-----------------");
            logToast(" AddUserToHomeWorker.java -> addMemberToHome():  calling saveInBackground:",  "-----------------");

            home.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e != null){
                        logToast("ERROR AddUserToHomeWorker.java -> addMemberToHome(): ",  e.getLocalizedMessage() + "-----------------");

                    }
                    else{
                        logToast(" AddUserToHomeWorker.java -> addMemberToHome(): ",  "saving home to arrayList-----------------");
                        saveHomeToArrayList(user, home);

                    }
                }
            });
        }
        else{//self explanitory
            logToast(" AddUserToHomeWorker.java -> addMemberToHome(): ",  "if statement called:false-----------------");
        }

    }

    public void saveHomeToArrayList(ParseUser user, ParseObject home){
        logToast("AddUserToHomeWorker.java -> saveHomeToArrayList(): ",  "Called!-----------------");
        logToast("AddUserToHomeWorker.java -> current UserObject list -> ",  user.getList("HomeList").toString()+ "-----------------");
        logToast("AddUserToHomeWorker.java -> saveHomeToArrayList(): adding -> ",  home.getObjectId()+ "-----------------");

        homesList.addAll(Objects.requireNonNull(user.getList("HomeList")));
        logToast("AddUserToHomeWorker.java ->  homeList -> ",  homesList.toString()+ "-----------------");
        homesList.add(home.getObjectId());
        logToast("AddUserToHomeWorker.java -> homeList updated -> ",  homesList.toString()+ "-----------------");

        user.put("HomeList", homesList);

        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e != null){
                    logToast("ERROR AddUserToHomeWorker.java -> saveHomeToArrayList(): ",  e.getLocalizedMessage() + "-----------------");

                }
                else{
                    logToast("AddUserToHomeWorker.java -> saveHomeToArrayList(): ",  "subscribingHomeFCMTopic" + "-----------------");
                    subscribeToHomeFCMTopic();
                }
            }
        });
    }

    public void subscribeToHomeFCMTopic(){
        logToast("AddUserToHomeWorker.java -> subscribeToHomeFCM(): ",  "Called!-----------------");
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (!task.isSuccessful()) {
                            logToast("AddUserToHomeWorker.java -> subscribeToHomeFCM(): ",  "Subscribe to home failed!-----------------");
                        }
                        else{
                            subscribeToPersonalTopic();

                        }

                    }
                });
    }
    public void subscribeToPersonalTopic(){
        logToast("AddUserToHomeWorker.java -> subscribeToPersonalTopic(): ",  "Called!-----------------");
        FirebaseMessaging.getInstance().subscribeToTopic(personalTopic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        user.put("HOMETOPIC",topic);
                        user.put("PERSONALTOPIC", personalTopic);
                        user.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e == null){
                                    logToast("AddUserToHomeWorker.java -> subscribeToPersonalTopic(): ",  "User should be successfully added to home!-----------------");
                                }
                                else{
                                    logToast("Error AddUserToHomeWorker.java -> subscribeToPersonalTopic(): ",  e.getLocalizedMessage()+"-----------------");
                                }

                                //buildJSONMessageObject();
                            }
                        });
                    }
                });
    }
    public void logToast(String tag, String text) {
        Log.d(tag, text);

    }
}
