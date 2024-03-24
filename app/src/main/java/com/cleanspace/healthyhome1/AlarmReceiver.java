package com.cleanspace.healthyhome1;

import static com.parse.Parse.getApplicationContext;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/*From github :
ardiibas/AlarmReceiver.class
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String TYPE_ONE_TIME = "OneTimeAlarm";
    public static final String EXTRA_MESSAGE = "message";
    public static final String EXTRA_TYPE = "type";
    private final int ID_ONETIME = 100;

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try{

            String message = intent.getStringExtra("taskDetails");
            String title = intent.getStringExtra("taskName");
            String type;
            int notifId = type.equalsIgnoreCase(TYPE_ONE_TIME) ? ID_ONETIME : 0;
            //int notifId = ID_ONETIME;
            if(notifId == ID_ONETIME){
                
            }
            /*
             * TODO: code is "trying to say"
             *  if(type == TYPE_ONE_TIME "ignore case"){
             *       notifId = ID_ONETIME <- means one time alarm
             *   }
             *  else{
             *       notifId = 0; <- means repeating alarm
             *   }
             *   but will fail if like this: int notifId = type.equalsIgnoreCase(TYPE_ONE_TIME) ? ID_ONETIME : 0;
             * */
            showToast(context, title, message);
            showAlarmNotification(context, title, message, notifId);
        }
        catch(Exception e){
            logToast("AlarmReceiver.java:", e.getLocalizedMessage());
        }
    }

    private void showToast(Context context, String title, String message) {
        Toast.makeText(context, title + " : " + message, Toast.LENGTH_LONG).show();
    }

    private void showAlarmNotification(Context context, String title, String message, int notifId) {
        String CHANNEL_ID = "Channel_1";
        String CHANNEL_NAME = "AlarmManager channel";
        NotificationManager notificationManagerCompat = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_home_black_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ContextCompat.getColor(context, android.R.color.transparent))
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setSound(alarmSound);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{1000, 1000, 1000, 1000, 1000});
            builder.setChannelId(CHANNEL_ID);
            if (notificationManagerCompat != null) {
                notificationManagerCompat.createNotificationChannel(channel);
            }
        }
        Notification notification = builder.build();
        if (notificationManagerCompat != null) {
            notificationManagerCompat.notify(notifId, notification);
        }
    }
    public void setOneTimeAlarm(Context context, String type, String date, String time, String message) {
        String DATE_FORMAT = "yyyy-MM-dd";
        String TIME_FORMAT = "HH:mm";
        if (isDateInvalid(date, DATE_FORMAT) || isDateInvalid(time, TIME_FORMAT)) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra(EXTRA_MESSAGE, message);
        intent.putExtra(EXTRA_TYPE, type);
        Log.e("ONE TIME", date + " " + time);
        String dateArray[] = date.split("-");
        String timeArray[] = time.split(":");
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(dateArray[0]));
        calendar.set(Calendar.MONTH, Integer.parseInt(dateArray[1]) - 1);
        calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateArray[2]));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArray[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArray[1]));
        calendar.set(Calendar.SECOND, 0);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ID_ONETIME, intent, PendingIntent.FLAG_IMMUTABLE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }
        Toast.makeText(context, "One time alarm set up", Toast.LENGTH_SHORT).show();
    }

    public boolean isDateInvalid(String date, String format) {
        try {
            DateFormat df = new SimpleDateFormat(format, Locale.getDefault());
            df.setLenient(false);
            df.parse(date);
            return false;
        } catch (ParseException e) {
            return true;
        }
    }
    public void logToast(String tag, String text) {
        Log.d(tag, text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }
}