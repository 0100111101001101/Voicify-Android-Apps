package com.research.voicify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    // NOTE: UI View outlets below here
    EditText speechTextOutlet;
    Button listenBtn;

    // NOTE: Global Variables below here
    List<String> argumentVector;
    int argumentCount = 0;

    // NOTE: Conditionals below here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();

        if (uri != null) {
            argumentVector = uri.getPathSegments();
            argumentCount = argumentVector.size();
            toastArgs();
            //Log.e("debug", Integer.toString(argumentCount));
        }

        // NOTE: Programmatically link UI elements here
        listenBtn = findViewById(R.id.listenBtn);
        speechTextOutlet = findViewById(R.id.speechTextEditText);

        // NOTE: Set listeners to Views here
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: google actions invoked here
            }
        });

    }

    private void toastArgs(){
        String toastMsg = "";
        for (int argIndex = 0; argIndex < argumentCount; argIndex++) {
            toastMsg = "Arg "+Integer.toString(argIndex+1)+": "+ argumentVector.get(argIndex);
            Toast.makeText(this,toastMsg,Toast.LENGTH_SHORT).show();
        }
    }
}

// Deep Link
//URL: app://www.voicifyApps.com/bigMac/burger/doordash
//URL: https://www.voicifyApps.com/bigMac/burger/doordash