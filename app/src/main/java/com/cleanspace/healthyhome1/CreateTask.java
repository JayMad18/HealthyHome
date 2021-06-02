package com.cleanspace.healthyhome1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class CreateTask extends AppCompatActivity {
    EditText taskNameEditText, userNameEditText;
    TextView taskNameTextView, assignToTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        taskNameEditText = findViewById(R.id.taskNameEditText);
        assignToTextView = findViewById(R.id.assignToTextView);
    }
    public void createTask(View view){
        //New task parse object here
    }
}