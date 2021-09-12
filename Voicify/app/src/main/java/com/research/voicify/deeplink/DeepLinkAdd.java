package com.research.voicify.deeplink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.research.voicify.R;

import java.util.HashSet;
import java.util.Set;

public class DeepLinkAdd extends AppCompatActivity {
    final String FILE_NAME = "voicify";
    final String ALL_COMMANDS = "all_commands";
    EditText commandTxt;
    EditText step1;

    Button addConfirmedBtn;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_link);

        commandTxt = findViewById(R.id.commandTxt);
        step1 = findViewById(R.id.step1);
        sharedPreferences = getSharedPreferences(FILE_NAME,0);      // open the SF that is used in this app
        SharedPreferences.Editor editor = sharedPreferences.edit();         // call an editor to modify SF
        Set<String> defaultSet = new HashSet<String>();
        Set<String> fetchedCommandSet = sharedPreferences.getStringSet(ALL_COMMANDS,defaultSet);

        addConfirmedBtn = (Button)  findViewById(R.id.addConfirmed);        // add button
        addConfirmedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(step1.getText() != null){

                    fetchedCommandSet.add(commandTxt.getText().toString());
                    editor.putStringSet(ALL_COMMANDS, fetchedCommandSet);       // using the editor.

                    String hashedSequence = step1.getText().toString();     // the string that will be pushed as list of commands

                    editor.putString(commandTxt.getText().toString(),hashedSequence);
                    editor.apply();         // apply changes
                    Intent myIntent = new Intent(DeepLinkAdd.this, DeepLinkList.class);     // after add, go back to the list
                    startActivity(myIntent);
                }

            }
        });
    }
}
