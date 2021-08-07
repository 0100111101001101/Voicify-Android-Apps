package com.research.voicify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // NOTE: UI View outlets below here
    EditText speechTextOutlet;
    Button listenBtn;

    // NOTE: Global Variables below here
    List<String> argumentVector;
    int argumentCount = 0;
    SpeechRecognizer speechRecognizer;
    Intent speechRecognizerIntent;
    String debugLogTag= "FIT4003_VOICIFY";                  // use this tag for all log tags.

    // NOTE: Conditionals below here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAudioPermission();
        initializeSpeechRecognition();                      // Checking permissions & initialising speech recognition

        Uri uri = getIntent().getData();

        if (uri != null) {
            argumentVector = uri.getPathSegments();
            argumentCount = argumentVector.size();          // deep linking example from WK1
            toastArgs();
        }

        // NOTE: Programmatically link UI elements here
        listenBtn = findViewById(R.id.listenBtn);
        speechTextOutlet = findViewById(R.id.speechTextEditText);

        // NOTE: Set listeners to Views here

        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(debugLogTag, "start");
                speechRecognizer.startListening(speechRecognizerIntent);       // on click listener to start listening audio
            }
        });

    }

    private void toastArgs(){
        /*
        * This function is used to toast all the arguments received from deep linking URI.
        *
        * @post-cond: It will toast all the received arguments onto the screen. (if any)
        */
        String toastMsg = "";
        for (int argIndex = 0; argIndex < argumentCount; argIndex++) {
            toastMsg = "Arg "+Integer.toString(argIndex+1)+": "+ argumentVector.get(argIndex);
            Toast.makeText(this,toastMsg,Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeSpeechRecognition() {
        /*
         * This function performs all the steps required for speech recognition initialisation
         */

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // also available: LANGUAGE_MODEL_WEB_SEARCH
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {

            @Override
            public void onReadyForSpeech(Bundle params) {
                // Called when the endpointer is ready for the user to start speaking.
            }

            @Override
            public void onBeginningOfSpeech() {
                // The user has started to speak.
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                // The sound level in the audio stream has changed.
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                // More sound has been received.
            }

            @Override
            public void onEndOfSpeech() {
                // Called after the user stops speaking
            }

            @Override
            public void onError(int error) {

                //Toast.makeText(MainActivity.this, "An error has occurred. Code: " + Integer.toString(error), Toast.LENGTH_SHORT).show();
                if(error == 8 || error == 7) {
                    speechRecognizer.cancel();
                    speechRecognizer.startListening(speechRecognizerIntent);
                }

            }

            @Override
            public void onResults(Bundle results) {
                // Called when recognition results are ready.
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches!=null) {
                    for (String match : matches) {
                        Log.d(debugLogTag, match);
                        String[] words = match.split(" ");
                        for (int index=0; index< words.length; index++) {
                            String word = words[index].toLowerCase().trim();
                            if (index == 0 ) {
                                if (!word.equals("open")) {
                                    break;
                                }
                            } else {
                                Log.d(debugLogTag,"other words  "+ word);
                                openApp(word);
                            }
                        }
                    }
                }
                speechRecognizer.startListening(speechRecognizerIntent);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                // Called when partial recognition results are available.
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                // reserved by android for future events
            }
        });
    }

    private void checkAudioPermission() {
        /*
         * This function checks the permissions and starts the settings activity for the given app
         * for the user to enable the required permissions for the app to run as intended. (These
         * permissions were required after android marshmallow.
         *
         * @post-cond: It will exit the app and start the settings screen with the app details on it.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)) {
                Intent permissionIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+ getPackageName()));
                startActivity(permissionIntent);
                Toast.makeText(this,"Please enable microphone access and relaunch.",Toast.LENGTH_LONG).show();
                finish();
            }

        }
    }

    private void openApp(String inputName) {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo packageInfo : packages) {
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();
                if(appName.contains(inputName) || inputName.contains(appName)){
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
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

// Deep Link
//URL: app://www.voicifyApps.com/bigMac/burger/doordash
//URL: https://www.voicifyApps.com/bigMac/burger/doordash

//--------------------------------------------------------------------------------------------------