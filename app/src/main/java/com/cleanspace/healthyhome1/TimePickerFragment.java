package com.cleanspace.healthyhome1;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.text.format.DateFormat;

import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.util.Calendar;

import static com.parse.Parse.getApplicationContext;

public class TimePickerFragment  extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    public int pm_am;
    private Object TimePickerFragment;
    private TimePicker TimePicker;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        TimePickerFragment = new TimePickerDialog(getActivity(), this, hour, minute, false);

        // Create a new instance of TimePickerDialog and return it
        return (Dialog) TimePickerFragment;



    }

    /**
     * Called when the user is done setting a new time and the dialog has
     * closed.
     *
     * @param view      the view associated with this listener
     * @param hourOfDay the hour that was set
     * @param minute    the minute that was set
     */
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Set a variable to hold the current time AM PM Status
        // Initially we set the variable value to AM
        String status = "AM";

        if(hourOfDay > 11)
        {
            // If the hour is greater than or equal to 12
            // Then the current AM PM status is PM
            status = "PM";
        }




        ((CreateTask) getActivity()).setTime(hourOfDay, minute, status);

    }
    public void logToast(String tag, String text){
        Log.d(tag,text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }



}