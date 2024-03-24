package com.cleanspace.healthyhome1;

import static com.parse.Parse.getApplicationContext;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IntervalFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IntervalFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IntervalFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IntervalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IntervalFragment newInstance(String param1, String param2) {
        IntervalFragment fragment = new IntervalFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    public void logToast(String tag, String text){
        Log.d(tag,text);
        Toast.makeText(getApplicationContext(), tag + ": " + text, Toast.LENGTH_LONG).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_interval, container, false);
        RadioGroup radioGroup = (RadioGroup) view.findViewById(R.id.intervalRadioGroup);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                switch(checkedId){
                    case R.id.dayRadioButton:
                        logToast("----------Checked radio button: ", checkedId + " -> " + "dayRadioButton");
                    case R.id.weekRadioButton:
                        logToast("----------Checked radio button: ", checkedId + " -> " + "weekRadioButton");
                    case R.id.monthRadioButton:
                        logToast("----------Checked radio button: ", checkedId + " -> " + "monthRadioButton");
                    case R.id.yearRadioButton:
                        logToast("----------Checked radio button: ", checkedId + " -> " + "yearRadioButton");
                    default:
                        logToast("----------Checked radio button: ", "No button check detected...");

                }
            }
        });


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_interval, container, false);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

    }
}