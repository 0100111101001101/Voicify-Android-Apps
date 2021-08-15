package com.research.voicify;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author: om mandavia (oman0003, 29145643) , minh duc vu (mvuu0003, )
 * @version: V1.1
 * @implNote: 15/08/2021
 */

public class VoiceToActionService extends AccessibilityService {

    FrameLayout mLayout;

    boolean isOn = false;
    SpeechRecognizer speechRecognizer;                      // declaring speech recognition var
    Intent speechRecognizerIntent;
    String debugLogTag= "FIT4003_VOICIFY";                  // use this tag for all log tags.

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }
        Log.d("Service Test","Child number: " + source.getChildCount());
//        setTextForAllSubNode(getRootInActiveWindow(), 0, event);
//        setText(source,event);
        //printOutElementTree(source);
        printOutAllClickableElement(getRootInActiveWindow(), 0, event);
    }


    public void printOutAllClickableElement(AccessibilityNodeInfo nodeInfo, int depth, AccessibilityEvent event){
        if (nodeInfo == null) return;
        if(nodeInfo.isClickable()){
            String label = " ";
            if (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null || event.getText().size() != 0) {
                if (nodeInfo.getText() != null) {
                    label += nodeInfo.getText();
                    label += " / ";
                }
                else if (event.getText().size() != 0) {
                    label += event.getText();
                    label += " / ";
                }
                else if (event.getContentDescription() != null) {
                    label += event.getContentDescription();
                    label += " / ";
                }
            }
            Log.d("Service Test",label);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            printOutAllClickableElement(nodeInfo.getChild(i), depth + 1, event);
        }
    }

    public void setTextForAllSubNode(AccessibilityNodeInfo nodeInfo, int depth, AccessibilityEvent event)  {
        if (nodeInfo == null) return;
        String logString = "";
        for (int i = 0; i < depth; ++i) {
            logString += " ";
        }
        logString += "Type: " + nodeInfo.getClassName() + " " + " Content-Description: " + nodeInfo.getContentDescription();
        //Log.v("Service Test", logString);
        if(nodeInfo.isEditable()){
            String label = " ";
            if (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null || event.getText().size() != 0) {
                Log.e("Service Test", "Existing description found");
                if (nodeInfo.getText() != null) {
                    label += nodeInfo.getText();
                    label += " / ";
                }
                else if (event.getText().size() != 0) {
                    label += event.getText();
                    label += " / ";
                }
                else if (event.getContentDescription() != null) {
                    label += event.getContentDescription();
                    label += " / ";
                }
            }
            // setPromptedText(nodeInfo, label);
            setAutoText(nodeInfo);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            setTextForAllSubNode(nodeInfo.getChild(i), depth + 1, event);
        }
    }
    public void setAutoText(AccessibilityNodeInfo currentNode){
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Set Text automatically");
        currentNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    public void setPromptedText(AccessibilityNodeInfo currentNode, String label)  {
        Bundle arguments = new Bundle();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(System.in));
        // Reading data using readLine
        String value = null;
        try {
            value = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value);
        currentNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    public void setTextForNode( AccessibilityNodeInfo currentNode, AccessibilityEvent event){
        if (currentNode != null & event.getClassName().equals("android.widget.EditText")) {
            //if(currentNode.isEditable()) Log.d("Service Test", "EDITABLE ");
            Log.d("Service Test", "Child auto set text ");
            setAutoText(currentNode);
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("Service Test","Service Disconnected");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);


        checkAudioPermission();
        initializeSpeechRecognition();                      // Checking permissions & initialising speech recognition
        configureListenButton();

        Log.d("Service Test","Service Connected");
    }

    private void configureListenButton() {
        Button listenBtn = (Button) mLayout.findViewById(R.id.listenBtn);
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listenBtn.getText().toString().equalsIgnoreCase("start")){
                    listenBtn.setText("Stop");
                    isOn = true;
                    speechRecognizer.startListening(speechRecognizerIntent);       // on click listener to start listening audio
                } else {
                    listenBtn.setText("Start");
                    isOn = false;
                    speechRecognizer.stopListening();
                }
            }
        });
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

                            // Adding some text-to-speech feedback for opening apps based on input
                            // Text-to-speech feedback if app not found);
                            speakerTask("Opening " + inputName);

                        } catch (ActivityNotFoundException err) {
                            // Text-to-speech feedback if app not found
                            speakerTask("I'm sorry. I couldn't find " + inputName);

                            // Render toast message on screen
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
            }

        }
    }

    // Use this method to call out to TTSService (Text-To-speech service) to speak out message
    public void speakerTask(String toSpeak) {
        Intent i = new Intent(this, TTSService.class);
        i.putExtra("message", toSpeak);
        // starts service for intent
        startService(i);
    }
}