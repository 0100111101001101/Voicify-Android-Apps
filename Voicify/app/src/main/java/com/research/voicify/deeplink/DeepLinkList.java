package com.research.voicify.deeplink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.research.voicify.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class DeepLinkList extends AppCompatActivity {
    RecyclerView recyclerView;
    final String FILE_NAME = "voicify";
    final String ALL_COMMANDS = "all_commands";
    SharedPreferences sharedPreferences;
    DeepLinkRecyclerAdapter adapter;
    ArrayList<DeepLinkItem> allDeepLinkItems = new ArrayList<>();
    Button addBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deeplink);

        recyclerView = findViewById(R.id.deeplink_list);        // set the recycler view with id
        recyclerView.addOnItemTouchListener(
                new RecyclerClickListener(getApplicationContext(), recyclerView ,new RecyclerClickListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        Intent myIntent = new Intent(DeepLinkList.this, DeepLinkAdd.class);
                        myIntent.putExtra("currentItem",allDeepLinkItems.get(position).title);
                        startActivity(myIntent);
                    }

                })
        );
        // invoking shared preferences for storing and retrieving data
        sharedPreferences = getSharedPreferences(FILE_NAME,0);
        Set<String> fetchedCommandSet = sharedPreferences.getStringSet(ALL_COMMANDS,null);
        // fetch the data if there is any predefined commands
        if(fetchedCommandSet != null){
            for(String title: fetchedCommandSet){
                String commands  = sharedPreferences.getString(title,null);
                ArrayList<String> allCommands = new ArrayList<>();                       // store all the steps
                Collections.addAll(allCommands, commands.split(";"));              // to execute the function
                DeepLinkItem deepLinkItem = new DeepLinkItem(title,allCommands);
                allDeepLinkItems.add(deepLinkItem);
            }
        }

        adapter = new DeepLinkRecyclerAdapter(allDeepLinkItems, new ClickListener() {
            @Override
            public void onPositionClicked(int position) {   // implement the delete button for items
                DeepLinkItem item = allDeepLinkItems.remove(position);
                SharedPreferences.Editor editor = sharedPreferences.edit();     // use the editor to change the data inside shared prefs
                editor.remove(item.title);
                fetchedCommandSet.remove(item.title);           // remove the deep link
                editor.putStringSet(ALL_COMMANDS, fetchedCommandSet);
                editor.apply();             // call apply to save the changes to shared preferences
                adapter.updateData(allDeepLinkItems);
            }

        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        addBtn = findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {      // implement the "add" button, go to the new activity.
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(DeepLinkList.this, DeepLinkAdd.class);
                startActivity(myIntent);
            }
        });
    }

}
