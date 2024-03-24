package com.cleanspace.healthyhome1;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.IBinder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.Objects;
import java.util.Random;

//Cant resolve symbol error on imports below
//import com.google.firebase.quickstart.fcm.R;
//
//import androidx.work.OneTimeWorkRequest;
//import androidx.work.WorkManager;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    public String selectedHomeObjectId;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        logToast("MyFireBaseMessagingService.java.java -> onMessagedReceived()", "----------------------------");
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ



        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            constructNotificationFromMessage(remoteMessage);
//            if (/* Check if data needs to be processed by long running job */ true) {
//                // For long-running tasks (10 seconds or more) use WorkManager.
//                //scheduleJob();
//            } else {
//                // Handle message within 10 seconds
//                handleNow(remoteMessage);
//            }
            if(remoteMessage.getData().get("alarmTimeMillis") != null){
                createAlarmWorkRequest(remoteMessage.getData().get("taskName"), remoteMessage.getData().get("taskDetails"), Integer.parseInt(Objects.requireNonNull(remoteMessage.getData()
                        .get("alarmTimeMillis"))));
                logToast("MyFireBaseMessagingService.java.java -> AlarmTimeMillis Not Null :)", "----------------------------");
            }
            else if (remoteMessage.getData().get("title").equals("Request")) {
                logToast("MyFireBaseMessagingService.java.java -> onMessageReceived() -> Request Not Null :)", "----------------------------");
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            constructNotificationFromMessage(remoteMessage);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]
    /**
     * There are two scenarios when onNewToken is called:
     * 1) When a new token is generated on initial app startup
     * 2) Whenever an existing token is changed
     * Under #2, there are three scenarios when the existing token is changed:
     * A) App is restored to a new device
     * B) User uninstalls/reinstalls the app
     * C) User clears app data
     */
    @Override
    public void onNewToken(String token) {

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        if(ParseUser.getCurrentUser() != null){
            sendRegistrationToServer(token);
        }

    }
    // [END on_new_token]

//    /**
//     * Schedule async work using WorkManager.
//     */
//    private void scheduleJob() {
//        // [START dispatch_job]
//        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(MyWorker.class)
//                .build();
//        WorkManager.getInstance(this).beginWith(work).enqueue();
//        // [END dispatch_job]
//    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow(RemoteMessage remoteMessage) {
        constructNotificationFromMessage(remoteMessage);
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any
     * server-side account maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        ParseUser user = ParseUser.getCurrentUser();
        user.put("FCMDeviceRegistrationToken",token);
        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                }
                else{
                }
            }
        });
    }

//    /**
//     * Create and show a simple notification containing the received FCM message.
//     *
//     * @param messageBody FCM message body received.
//     */
//    private void sendNotification(String messageBody) {
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
//                PendingIntent.FLAG_ONE_SHOT);
//
//        //String channelId = getString(R.string.default_notification_channel_id);
//        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        NotificationCompat.Builder notificationBuilder =
//                new NotificationCompat.Builder(this, channelId)
//                        .setSmallIcon(R.drawable.ic_stat_ic_notification)
//                        .setContentTitle(getString(R.string.fcm_message))
//                        .setContentText(messageBody)
//                        .setAutoCancel(true)
//                        .setSound(defaultSoundUri)
//                        .setContentIntent(pendingIntent);
//
//        NotificationManager notificationManager =
//                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//
//        // Since android Oreo notification channel is needed.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(channelId,
//                    "Channel human readable title",
//                    NotificationManager.IMPORTANCE_DEFAULT);
//            notificationManager.createNotificationChannel(channel);
//        }
//
//        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
//    }
public void createAlarmWorkRequest(String taskName, String taskDetails, long alarmTimeMillis){
    WorkRequest CreateAlarmWorkRequest = new OneTimeWorkRequest.Builder(
            CreateAlarmWorker.class).setInputData(new Data.Builder()
                    .putString("taskName", taskName)
                    .putString("taskDetails", taskDetails)
                    .putLong("alarmTimeMilli", alarmTimeMillis)
                    //.putLong("type",type)
                    .build())
            .build();

    WorkManager.getInstance(getApplicationContext()).enqueue(CreateAlarmWorkRequest);
    logToast("CreateAlarmWorkRequest Enqueued!!!", "----------------------------");
}

    private void constructNotificationFromMessage(RemoteMessage remoteMessage){

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
        setupChannels(notificationManager);
      /*
        Apps targeting SDK 26 or above (Android O) must implement notification channels and add its notifications
        to at least one of them. Therefore, confirm if version is Oreo or higher, then setup notification channel
      */



        final Intent sendTaskInfo = new Intent(this, HomeScreen.class);
        Log.d(" MyFireBaseMessagingService.java.java -> Received Intent:", sendTaskInfo +"--------------");
        if(remoteMessage.getData().get("HomeObjectID") == null){
            logToast("MyFireBaseMessagingService.java.java -> constructNotificationFromMessage(RemoteMessage remoteMessage) -> HomeObjectID", "is null--------------");
        }
        else{
            logToast("MyFireBaseMessagingService.java.java -> construct..()) -> HomeObjectID", remoteMessage.getData().get("HomeObjectID")+"--------------");
            sendTaskInfo.putExtra("HomeObjectID", remoteMessage.getData().get("HomeObjectID"));

            if(remoteMessage.getData().get("alarmTimeMillis") != null){
                sendTaskInfo.putExtra("notification",remoteMessage.getData().get("HomeObjectID"));
                logToast("MyFireBaseMessagingService.java.java -> construct..() -> alarmTimeMillis Not Null :)", "----------------------------");
            }

            else if (remoteMessage.getData().get("title").equals("Request")) {
                logToast("MyFireBaseMessagingService.java.java -> construct..() -> Request Not Null :)", "----------------------------");
                sendTaskInfo.putExtra("Request",(remoteMessage.getData().get("title")));
                sendTaskInfo.putExtra("HomeObjectID",remoteMessage.getData().get("HomeObjectID"));
                logToast("MyFirebaseMessagingService.java -> construct..() received home objectId:", remoteMessage.getData().get("HomeObjectID"));
                sendTaskInfo.putExtra("requestedUserObjectID",remoteMessage.getData().get("requestedUserObjectID"));
                sendTaskInfo.putExtra("topic",remoteMessage.getData().get("topic"));
                sendTaskInfo.putExtra("personalTopic",remoteMessage.getData().get("personalTopic"));
            }
            else if (remoteMessage.getData().get("title").equals("Accepted!") ) {
                addUserToHomeWorkRequest(remoteMessage.getData().get("HomeObjectID"), remoteMessage.getData().get("requestedUserObjectID")
                        , remoteMessage.getData().get("topic")
                        ,remoteMessage.getData().get("personalTopic"));

                logToast("MyFireBaseMessagingService.java.java -> construct..() -> Join Request :", "ACCEPTED----------------------------");
            }
            else if(remoteMessage.getData().get("title").equals("Denied..")){
                logToast("MyFireBaseMessagingService.java.java -> construct..() -> Join Request :", "DENIED----------------------------");
            }
            sendTaskInfo.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }



        PendingIntent pendingIntent = PendingIntent.getActivity(this , notificationID, sendTaskInfo,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);


//        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
//                R.drawable.notify_icon);

        Uri notificationSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.general_channel_id))
                .setSmallIcon(R.drawable.ic_home_black_24dp)
//                .setLargeIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle(remoteMessage.getData().get("title"))
                .setContentText(remoteMessage.getData().get("message"))
                .setAutoCancel(true)
                .setSound(notificationSoundUri)
                .setContentIntent(pendingIntent);


        //Set notification color to match your app color template
        notificationBuilder.setColor(getResources().getColor(R.color.Pink));
        notificationManager.notify(notificationID, notificationBuilder.build());
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager){
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device to device notification";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(getString(R.string.general_channel_id), adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
    public void addUserToHomeWorkRequest(String foundHomeObjectID, String requestedUserObjectID, String topic, String personalTopic){
        WorkRequest addUserToHomeWorkRequest = new OneTimeWorkRequest.Builder(
                AddUserToHomeWorker.class).setInputData(new Data.Builder()
                        .putString("foundHomeObjectID", foundHomeObjectID)
                        .putString("requestedUserObjectID", requestedUserObjectID)
                        .putString("topic", topic)
                        .putString("personalTopic", personalTopic)
                        .build())
                .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(addUserToHomeWorkRequest);
        logToast("addUserToHomeWorkRequest Enqueued!!!", "----------------------------");
    }
    public void logToast(String tag, String text) {
        Log.d(tag, text);

    }


}