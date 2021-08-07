package com.research.voicify;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AppOpenerActivity extends AppCompatActivity {
    EditText appName; // View to get the text to be analyzed
    Button startApp;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_opener);
        appName = findViewById(R.id.name);
        startApp = findViewById(R.id.startApp);
        startApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PackageManager pm = getPackageManager();
//get a list of installed apps.
                List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                String inputName = appName.getText().toString().toLowerCase().trim();
                for (ApplicationInfo packageInfo : packages) {
                    try {
                        ApplicationInfo info = pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                        String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();
                        if(appName.contains(inputName)){
                            Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                                    packageInfo.packageName);
                            if (mIntent != null) {
                                try{
                                    startActivity(mIntent);
                                } catch (ActivityNotFoundException err) {
                                    Toast t = Toast.makeText(getApplicationContext(),
                                            "APP NOT FOUND", Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            }
                        }
                        //Log.d("AppOpener", "App name :" + appName);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }


                }
            }
        });
    }
}
