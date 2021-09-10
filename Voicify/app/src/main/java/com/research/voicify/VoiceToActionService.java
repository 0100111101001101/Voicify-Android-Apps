package com.research.voicify;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author: Om Harish Mandavia (oman0003, 29145643), Minh Duc Vu (mvuu0003, ), Alex Dumitru(adum6, 27820289)
 * @version: V1.1
 * @implNote: last updated on 15/08/2021
 */


@RequiresApi(api = Build.VERSION_CODES.R)
public class VoiceToActionService extends AccessibilityService {
    AccessibilityNodeInfo currentSource = new AccessibilityNodeInfo();
    ArrayList<AccessibilityNodeInfo> scrollableNodes = new ArrayList<AccessibilityNodeInfo>();
    FrameLayout mLayout;
    private String[] writtenNumbers = new String[]{"0","1","2","3","4","5","6","7","8","9","10","11","12","13","14"};
    ArrayList<FrameLayout> tooltipLayouts = new ArrayList<FrameLayout>();
    //private ArrayList<Pair<String,AccessibilityNodeInfo>> clickableNodes = new ArrayList<Pair<String, AccessibilityNodeInfo>>();
    private ArrayList<AccessibilityNodeInfo> unlabeledNodes = new ArrayList<>();
    boolean isOn = false;
    SpeechRecognizer speechRecognizer;                      // declaring speech recognition var
    Intent speechRecognizerIntent;
    String debugLogTag= "FIT4003_VOICIFY";                  // use this tag for all log tags.
    ArrayList<String> launchTriggers = new ArrayList<String>(Arrays.asList("load","start","launch","execute","open"));
    String[] pressTriggers = new String[]{"press","click"};

    private static final Map<String,String> speechPrompt=new HashMap<String,String>();  //Creating HashMap for TTS phrases these are used as shortcuts for common Text-to-speech strings
    static {
        speechPrompt.put("required","Please fill in the required fields");
        speechPrompt.put("multiple","Multiple options detected, which did you want to select?");
        speechPrompt.put("noInput","I'm sorry, can you please repeat that");
        speechPrompt.put("noMatch","I'm sorry, I couldn't find ");
        speechPrompt.put("open","Opening ");
        speechPrompt.put("scrollUp","Scrolling up");
        speechPrompt.put("scrollDown","Scrolling down");
        speechPrompt.put("stop", "Stopping voicify");
    }

    private int currentTooltipCount = 0;
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        /**
         * This function will be invoked when defined type of event occurs
         * param: event is an instance that capture every information about the event
         */

        // basic checks for null safety
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) {
            return;
        }

        removeAllTooltips();    // remove all old  tooltip when screen changed
        //clickableNodes.clear();
        currentSource = getRootInActiveWindow(); // update the current root node
        printOutAllClickableElement(getRootInActiveWindow(), 0, event); // call function for root node

    }

    public void getScrollableNode(AccessibilityNodeInfo currentNode){
        if (currentNode == null) return;
        if(currentNode.isClickable()){
            scrollableNodes.add(currentNode);
        }
        for (int i = 0; i < currentNode.getChildCount(); ++i) {
            getScrollableNode(currentNode.getChild(i));    // recursive call
        }
    }


    public void printOutAllClickableElement(AccessibilityNodeInfo nodeInfo, int depth, AccessibilityEvent event){
        /**
         * This function will print out all clickable element, storing the data it has or number for those
         * clickable elements.
         *
         */
        if (nodeInfo == null) return;
        if(nodeInfo.isClickable()){
            String label = "";
            if (nodeInfo.getText() != null || nodeInfo.getContentDescription() != null || event.getText().size() != 0) {
                if (nodeInfo.getText() != null) {   // check if node has a corresponding text
                    label += nodeInfo.getText();
                }
                else if (event.getText().size() != 0) { // check if event has text
                    label += event.getText();
                }
                else if (event.getContentDescription() != null) {   //check if event has content description
                    label += event.getContentDescription();
                }
                else if (nodeInfo.getTooltipText() != null){
                    label += nodeInfo.getTooltipText();
                } else {        // no information about node or event
                    if(currentTooltipCount<15) {
                        Rect rectTest = new Rect();                     //  to get the coordinate of the UI element
                        nodeInfo.getBoundsInScreen(rectTest);           //  store data of the node
                        inflateTooltip(rectTest.left, rectTest.top);    // call function to create number tooltips
                        unlabeledNodes.add(nodeInfo);                   // add to the list to retrieve later
                        Log.d(debugLogTag, currentTooltipCount+ ": " + rectTest.top + " " + rectTest.left);
                        currentTooltipCount += 1;
                    }
                }
            }
            //clickableNodes.add(new Pair<>(label,nodeInfo));
            Log.d(debugLogTag,"Available commands: " + label);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            printOutAllClickableElement(nodeInfo.getChild(i), depth + 1, event);    // recursive call
        }
    }

    public void setTextForAllSubNode(AccessibilityNodeInfo nodeInfo, int depth, AccessibilityEvent event)  {
        /**
         * This function will set text for all sub-node -- in experiment.
         */
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
        /**
         * This function is in experiment
         */
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "Set Text automatically");
        currentNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }

    public void setPromptedText(AccessibilityNodeInfo currentNode, String label)  {
        /**
         * This function is in experiment
         */
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
        /**
         * This function is in experiment
         */
        if (currentNode != null & event.getClassName().equals("android.widget.EditText")) {
            //if(currentNode.isEditable()) Log.d("Service Test", "EDITABLE ");
            Log.d("Service Test", "Child auto set text ");
            setAutoText(currentNode);
        }
    }

    public void scrollingActivity(String command){

        getScrollableNode(currentSource);
        if(scrollableNodes.size() == 0) Log.d(debugLogTag,"Can't find item to scroll");
        else {
            for(AccessibilityNodeInfo node: scrollableNodes){
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

                final int height = displayMetrics.heightPixels;
                final int top = (int) (height * .25);
                final int mid = (int) (height * .5);
                final int bottom = (int) (height * .75);
                final int midX = displayMetrics.widthPixels / 2;

                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                Path path = new Path();
                if (command.contains("up")) {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, bottom);

                } else {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, top);
                }
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 300));
                dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        Log.d(debugLogTag,"Gesture Completed");
                        super.onCompleted(gestureDescription);
                    }
                }, null);
            }


        }
    }

    @Override
    public void onInterrupt() {
        Log.d("Service Test","Service Disconnected");
    }

    @Override
    protected void onServiceConnected() {
        /*
        * This function is invoked after the accessibility service has been stared by the user. this
        * function inflates the layout and draws the floating UI for the service. It also initialises
        * speech recognition & checks audio permissions.
        *
        * @param: None
        * @return: None
        * @post-cond: A button floating on top of the screen can be used to control the service
        *             by the user if the app have all the permissions it needs. Else opens settings
        *             page with the app's details.
        * */

        super.onServiceConnected();

        createSwitch();
        checkAudioPermission();
        initializeSpeechRecognition();                      // Checking permissions & initialising speech recognition
        configureListenButton();

        Log.d("Service Test","Service Connected");
    }

    private void createSwitch(){
        /**
         * This code will create a layout for the switch. This code is called whenever service is
         * connected and will be gone when service is shutdown
         *
         */
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE); //
        mLayout = new FrameLayout(this);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.gravity = Gravity.TOP ;  // stick it to the top
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, lp);       // add it to the screen
    }

    private void inflateTooltip(int x, int y){
        /**
         * This function will configure each of the tooltip on the screen, so this function will be
         * called for each of the tooltip on the screen.
         * param: x is the location in x axis
         * param: y is the location in y axis
         */
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);    // window manager
        FrameLayout tooltipLayout = new FrameLayout(this);      // create new layout for each tooltip
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP|Gravity.START;     // reset the (0,0) to the top left screen
        lp.x = x;       // x location
        lp.y = y;       // y location
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.tooltip_number, tooltipLayout);   // inflate the view to the screen
        wm.addView(tooltipLayout, lp);

        TextView tooltip = tooltipLayout.findViewById(R.id.tooltip);    // set the count based on current count
        tooltip.setText(currentTooltipCount + "");
        Log.d(debugLogTag,"Inflated tooltip: " + currentTooltipCount);
        tooltipLayouts.add(tooltipLayout);  // add the tooltip for removing later to the arraylist
    }

    private void removeAllTooltips(){
        /**
         * This function will be called when something changed on the screen, reset all tooltips.
         *
         */
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        for (FrameLayout tooltipLayout: tooltipLayouts){    // remove the list of current tooltips
            if(tooltipLayout != null)
                wm.removeView(tooltipLayout);   // remove them from the screen
        }
        // reset all variables when changing to new screen.
        tooltipLayouts.clear();
        currentTooltipCount = 0;
        unlabeledNodes.clear();
    }

    private void configureListenButton() {
        /*
         * This function is called after the service has been connected. This function binds
         * functionality to the master button which can be used to turn on/off the tool.
         *
         * @param: None
         * @return: None
         * @post-cond: functionality has been added to the inflated button
         * */

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
                    speechRecognizer.stopListening();           // on click listener to stop listening & processing data
                }
            }
        });
    }

    private void openApp(String inputName) {
        /*
         * This function is used to check if the given string matches with any applications that the
         * user may have installed. It launches apps that have matched. Current matching algorithm is
         * trivial. (WIP: Improved Matching Algorithm)
         *
         * @param: inputName — This is a String that is supposed to be checked for app name matching
         * @return: None
         * @post-cond: Apps that match with the given string are launched and presented on the
         *             foreground adding them to the system backstack if multiple apps are launched.
         * */

        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // getting meta data of all installed apps

        for (ApplicationInfo packageInfo : packages) {          // checking if the input has a match with app name
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
                            speakerTask(speechPrompt.get("open") + inputName);

                        } catch (ActivityNotFoundException err) {
                            // Text-to-speech feedback if app not found
                            speakerTask(speechPrompt.get("noMatch") + inputName);

                            // Render toast message on screen
                            Toast t = Toast.makeText(getApplicationContext(),
                                    "APP NOT FOUND", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();                // handling app not found exception
                speakerTask(speechPrompt.get("noMatch") + inputName);
            }
        }
    }

    private void checkAudioPermission() {
        /*
         * This function checks the permissions and starts the settings activity for the given app
         * for the user to enable the required permissions for the app to run as intended. (These
         * permissions were required after android marshmallow.
         *
         * @param: None
         * @return: None
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

    private boolean isLaunchTrigger(String word) {
        /*
         * This function checks the permissions and starts the settings activity for the given app
         * for the user to enable the required permissions for the app to run as intended. (These
         * permissions were required after android marshmallow.
         *
         * @param: word - This is a String which is supposed to be checked against the trigger words.
         * @return: It returns a boolean evaluating the above mentioned check.
         * @post-cond: None
         */
        
        for (String launchTrigger : launchTriggers) {
            if (word.equals(launchTrigger))
                return true;
        }
        return false;
    }
    private boolean isPressTrigger(String word) {
        /**
         * This function checks the permissions and starts the settings activity for the given app
         * for the user to enable the required permissions for the app to run as intended. (These
         * permissions were required after android marshmallow.
         *
         * @param: word - This is a String which is supposed to be checked against the trigger words.
         * @return: It returns a boolean evaluating the above mentioned check.
         * @post-cond: None
         */
        for (String pressTrigger:pressTriggers){
            if(word.equals(pressTrigger))
                return true;
        }
        return false;
    }

    // Use this method to call out to TTSService (Text-To-speech service) to speak out message
    public void speakerTask(String toSpeak) {
        Intent i = new Intent(this, TTSService.class);
        i.putExtra("message", toSpeak);
        // starts service for intent
        startService(i);
    }

    boolean clickButtonByText(String word) {
        /**
         * This function will click a button (anything thats clickable) with provided information
         * param: word: a string to store data about what to click
         */

        for (int i = 0; i < writtenNumbers.length; i++) {  // finding matching strings for numbers
            if (word.trim().toLowerCase().equals(writtenNumbers[i])) {
                if (unlabeledNodes.size() > i && unlabeledNodes.get(i).performAction(AccessibilityNodeInfo.ACTION_CLICK))    // perform click with condition to return once click is successful
                    Log.d(debugLogTag, "Clicked on: " + i);    // log the information
                return true;
            }

        }
        //Find ALL of the nodes that match the "text" argument.
        List<AccessibilityNodeInfo> list = currentSource.findAccessibilityNodeInfosByText(word);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                Log.d(debugLogTag, "Clicked on:" + word);
            return true;     // return once clicked
        }
        // for some element that named with first capital word
        String camelCaseWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        list = currentSource.findAccessibilityNodeInfosByText(camelCaseWord);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK))
                Log.d(debugLogTag, "Clicked on:" + word);
            return true;     // return once clicked
        }
        return false;
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
                if(!isOn) {
                    return;
                }
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches!=null) {
                    for (String match : matches) {
                        Log.d(debugLogTag, match);
                        String[] words = match.split(" ");
                        ArrayList<String> trimmedWords = new ArrayList<String>();
                        // ["open","uber"...]
                        Log.d(debugLogTag,match);
                        for (int index=0; index< words.length; index++) {
                            String word = words[index].toLowerCase().trim();
                            trimmedWords.add(word);
                        }
                        boolean isActionInvoked = false;
                        String initialWord = trimmedWords.get(0) ; // first word from the command
                        if (initialWord.equals("back")){
                            performGlobalAction(GLOBAL_ACTION_BACK);
                            isActionInvoked = true;
                        } else if (initialWord.equals("home")){
                            performGlobalAction(GLOBAL_ACTION_HOME);
                            isActionInvoked = true;
                        } else if(launchTriggers.contains(initialWord)){
                            isActionInvoked = true;
                            for(int i = 1;i < trimmedWords.size(); i++)
                                openApp(trimmedWords.get(i));
                        } else if (initialWord.equals("scroll")){
                            scrollingActivity(trimmedWords.get(1));
                            isActionInvoked = true;
                        } else {
                            Log.d(debugLogTag,trimmedWords.get(0));
                            if (clickButtonByText(trimmedWords.get(0))) {
                                isActionInvoked = true;
                            }
                        }
                        if (isActionInvoked){
                            AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
                            if (manager.isEnabled()) {
                                AccessibilityEvent e = AccessibilityEvent.obtain();
                                e.setEventType(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
                                e.setClassName(getClass().getName());
                                e.getText().add("User interaction invoked this event");
                                manager.sendAccessibilityEvent(e);
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
}