package com.research.voicify;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.text.TextUtils;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.research.voicify.elements.LabelFoundNode;
import com.research.voicify.elements.TooltipRequiredNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.Calendar;

/**
 * @author: Om Harish Mandavia (oman0003, 29145643), Minh Duc Vu (mvuu0003, ), Alex Dumitru(adum6, 27820289)
 * @version: V1.1
 * @implNote: last updated on 15/08/2021
 */


@RequiresApi(api = Build.VERSION_CODES.R)
public class VoiceToActionService extends AccessibilityService {
    final String FILE_NAME = "voicify";
    final String ALL_COMMANDS = "all_commands";
    int width,height;
    SharedPreferences sharedPreferences;
    ArrayList<String> predefinedCommands = new ArrayList<>();
    ArrayList<String> currentSequence = new ArrayList<String>();
    AccessibilityNodeInfo currentSource = new AccessibilityNodeInfo();
    ArrayList<AccessibilityNodeInfo> scrollableNodes = new ArrayList<AccessibilityNodeInfo>();
    FrameLayout mLayout;
    ArrayList<LabelFoundNode> foundLabeledNodes = new ArrayList<>();
    ArrayList<TooltipRequiredNode> tooltipRequiredNodes = new ArrayList<>();
    boolean isOn = false;
    SpeechRecognizer speechRecognizer;                      // declaring speech recognition var
    Intent speechRecognizerIntent;
    String debugLogTag= "FIT4003_VOICIFY";                  // use this tag for all log tags.
    ArrayList<String> launchTriggers = new ArrayList<String>(Arrays.asList("load","start","launch","execute","open"));
    String[] pressTriggers = new String[]{"press","click"};
    WindowManager wm;
    long currentTime;
    //Creating HashMap for TTS phrases these are used as shortcuts for common Text-to-speech strings
    private static final Map<String,String> speechPrompt=new HashMap<String,String>();
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

    ArrayList<String> savedCommands = new ArrayList<>();

    private int currentTooltipCount = 1;
    boolean isRecording = false;
    boolean isPlaying = false;
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
        if(isNotBlockedEvent()) {
            removeAllTooltips();    // remove all old  tooltip when screen changed
            currentSource = getRootInActiveWindow(); // update the current root node
            printOutAllClickableElement(getRootInActiveWindow(), 0, event); // call function for root node
            autoExecutePredefinedCommand();
        }

    }

    public boolean isNotBlockedEvent(){
        Date date = new Date();
        long time = date.getTime();
        if (time - currentTime > 500){
            currentTime = time;
            return true;
        }
        Log.d(debugLogTag, "Event blocked for repetitive calls");
        return false;
    }

    public void autoExecutePredefinedCommand(){
        /**
         * This function will be triggered to execute any predefined set of action that has been started.
         */
        if(isPlaying){  // check if any sequence is triggered
            if(currentSequence.size() == 0){       // check if there is still element to execute
                Log.d(debugLogTag,"no more item to press");
                isPlaying = false;      // finished the sequence
            }
            else if(commandExecution(currentSequence.get(0))){ // execute the oldest
                currentSequence.remove(0);     // remove it if successful
                Log.d(debugLogTag,"clicked based on saved actions, items left: " + currentSequence.size());
                isPlaying = true;       // keep playing true for next invocation
            }
        }
    }

    public void getScrollableNode(AccessibilityNodeInfo currentNode){
        /**
         * Get all the scrollable node in the current screen.
         * @param: currentNode: the current node that is being checked ( start from root node and recursively for all node)
         */
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
                if (nodeInfo.getText() != null) {   // check if node has a corresponding text
                    label += nodeInfo.getText();
                     Log.d(debugLogTag,"Available commands: " + label);
                    return;
                } else {
                    // no information about node or event (Tags to be assigned!)
                    String foundLabel  = searchForTextView(nodeInfo,"");
                    if (!foundLabel.equals("")){
                        foundLabeledNodes.add(new LabelFoundNode(nodeInfo,foundLabel.toLowerCase()));
                        Log.d(debugLogTag,"Available commands: " +foundLabel.toLowerCase());
                    } else {
                        Rect rectTest = new Rect();                     //  to get the coordinate of the UI element
                        nodeInfo.getBoundsInScreen(rectTest);           //  store data of the node
                         if(rectTest.right < width && rectTest.bottom<height){
                            Log.d(debugLogTag, currentTooltipCount+ ": Left " + rectTest.left + " Top " + rectTest.top+ " Right " + rectTest.right + " Bottom " + rectTest.bottom);
                            inflateTooltip((rectTest.right+rectTest.left)/2, rectTest.top, nodeInfo);    // call function to create number tooltips

                        }
                    }
                }

            //clickableNodes.add(new Pair<>(label,nodeInfo));
           // Log.d(debugLogTag,"Available commands: " + label);
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {
            printOutAllClickableElement(nodeInfo.getChild(i), depth + 1, event);    // recursive call
        }
    }

    public String searchForTextView(AccessibilityNodeInfo currentNode, String allTexts){
        String concatenatedString = allTexts;
        if(currentNode == null || concatenatedString.split(" ").length > 5) return concatenatedString;
        if(currentNode.getClassName().equals("android.widget.TextView") && currentNode.getText() != null){
            concatenatedString += currentNode.getText().toString() + " ";
        } else {
            for (int i = 0; i < currentNode.getChildCount(); ++i) {
                concatenatedString += searchForTextView(currentNode.getChild(i),concatenatedString);    // recursive call
            }
        }
        return concatenatedString;
    }


    public void setTextForAllSubNode(AccessibilityNodeInfo nodeInfo, int depth, String text)  {
        /**
         * This function will set text for all sub-node ( all element on the screen)
         * @param: nodeInfo : current node that this function is called on ( will start from root node)
         * @param: depth : the current level of leaf
         * @param: text: the passed in text for writing in the edit text field.
         */
        if (nodeInfo == null) return;   // null check
        if(nodeInfo.isEditable()){      // check if the node has editable field
            setGivenText(nodeInfo,text);       // call a method to put in the text
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {    // recursive call to reach all nested nodes/leaves
            setTextForAllSubNode(nodeInfo.getChild(i), depth + 1, text);
        }
    }

    public void setGivenText(AccessibilityNodeInfo currentNode, String text){
        /**
         * This function will set the text for a given node
         * @param: currentNode: the node to store information about object that will be inserted the text.
         * @param: text: the customized passed in text to be written in the field.
         */
        Bundle arguments = new Bundle();
        arguments.putCharSequence(AccessibilityNodeInfo
                .ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,text);
        currentNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
    }



    public void scrollingActivity(String command){
        /**
         * This function will work as scrolling the screen for user on invocation.
         * @param: a string - can be up or down specifying the scrolling direction.
         */
        getScrollableNode(currentSource);   // get all scrollable not within current screen
        if(scrollableNodes.size() == 0) Log.d(debugLogTag,"Can't find item to scroll");
        else {      // if there exist item to be scrolled.
            for(AccessibilityNodeInfo node: scrollableNodes){
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                // scrolling using gesture builder.
                final int height = displayMetrics.heightPixels;
                final int top = (int) (height * .25);
                final int mid = (int) (height * .5);
                final int bottom = (int) (height * .75);
                final int midX = displayMetrics.widthPixels / 2;
                final int width = displayMetrics.widthPixels;
                final int left = (int) (width * 0.25);
                final int right = (int) (width * 0.75);

                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                Path path = new Path();


                // Scroll up
                if (command.contains("up")) {
                    path.moveTo(midX, mid);
                    path.lineTo(midX, bottom);
                // Scroll down
                } else if (command.contains("down")){
                    path.moveTo(midX, mid);
                    path.lineTo(midX, top);
                } else if (command.contains("right")) {
                    path.moveTo(right, mid);
                    path.lineTo(left, mid);
                } else if (command.contains("left")) {
                    path.moveTo(left, mid);
                    path.lineTo(right, mid);
                }

                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 100, 300));
                dispatchGesture(gestureBuilder.build(), new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) { // gesture execution
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
        /**
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
        wm  = (WindowManager) getSystemService(WINDOW_SERVICE);
        Date date = new Date();
        currentTime = date.getTime();
        createSwitch();
        checkAudioPermission();
        initializeSpeechRecognition();                      // Checking permissions & initialising speech recognition
        configureListenButton();
        configureRecordButton();
        configurePlayButton();
        loadPresavedCommands();
        getDisplayMetrics();
        Log.d("Service Test","Service Connected");
    }

    private void getDisplayMetrics() {
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
         width = metrics.widthPixels;
         height = metrics.heightPixels;
    }

    private void loadPresavedCommands(){
        sharedPreferences = getSharedPreferences(FILE_NAME,0);
        Set<String> fetchedCommandSet = sharedPreferences.getStringSet(ALL_COMMANDS,null);
        if (fetchedCommandSet != null) {
            predefinedCommands.addAll(fetchedCommandSet);
        }
    }

    private void createSwitch(){
        /**
         * This code will create a layout for the switch. This code is called whenever service is
         * connected and will be gone when service is shutdown
         *
         */
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


    private void inflateTooltip(int x, int y, AccessibilityNodeInfo nodeInfo){
        /**
         * This function will configure each of the tooltip on the screen, so this function will be
         * called for each of the tooltip on the screen.
         * param: x is the location in x axis
         * param: y is the location in y axis
         */
          // window manager
        FrameLayout tooltipLayout = new FrameLayout(this);      // create new layout for each tooltip
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        lp.format = PixelFormat.TRANSLUCENT;
        lp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP|Gravity.START;     // reset the (0,0) to the top left screen
        lp.x = x;       // x location
        lp.y = y - 100;       // y location
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.tooltip_number, tooltipLayout);   // inflate the view to the screen
        wm.addView(tooltipLayout, lp);

        TextView tooltip = tooltipLayout.findViewById(R.id.tooltip);    // set the count based on current count
        tooltip.setText(currentTooltipCount + "");
                 // add to the list to retrieve later

         tooltipRequiredNodes.add(new TooltipRequiredNode(nodeInfo,currentTooltipCount,tooltipLayout));
         currentTooltipCount += 1;

    }

    private void removeAllTooltips(){
        /**
         * This function will be called when something changed on the screen, reset all tooltips.
         *
         */
        for (TooltipRequiredNode tooltip: tooltipRequiredNodes){    // remove the list of current tooltips
            if(tooltip.tooltipLayout != null)
                wm.removeView(tooltip.tooltipLayout);   // remove them from the screen
        }
        // reset all variables when changing to new screen.
        currentTooltipCount = 1;
        tooltipRequiredNodes.clear();
        foundLabeledNodes.clear();
    }

    private void configureListenButton() {
        /**
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
    private void configureRecordButton() {

        Button listenBtn = (Button) mLayout.findViewById(R.id.recordBtn);
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listenBtn.getText().toString().equalsIgnoreCase("record")){
                    listenBtn.setText("Save");
                    isRecording = true;
                    savedCommands.clear();

                } else {
                    listenBtn.setText("Record");
                    isRecording = false;
                    if (savedCommands.size() >0){
                        String commands = "";
                        for(String command: savedCommands){
                            commands += command + ";";
                        }
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        predefinedCommands.add("new command");
                        Set<String> newCommands = new HashSet<>(predefinedCommands);
                        editor.putStringSet(ALL_COMMANDS,newCommands);
                        editor.putString("new command",commands);
                        editor.apply();
                    }

                    Log.d(debugLogTag," " + savedCommands.size());
                    Log.d(debugLogTag," " + savedCommands);
                }
            }
        });
    }
    private void configurePlayButton() {

        Button listenBtn = (Button) mLayout.findViewById(R.id.playBtn);
        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              commandExecution("new command");

            }
        });
    }

    private void openApp(String inputName) {
        /**
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
        /**
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
        /**
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


    public void speakerTask(String toSpeak) {
        /**
         * Use this method to call out to TTSService (Text-To-speech service) to speak out message
         * param: a string to be spoken by the Text-to-speech service
         */
        Intent i = new Intent(this, TTSService.class);
        i.putExtra("message", toSpeak);
        // starts service for intent
        startService(i);
    }

    public void findNearbyMapDeepLink(String location){
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+location);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    boolean clickButtonByText(String word) {
        /**
         * This function will click a button (anything thats clickable) with provided information
         * param: word: a string to store data about what to click
         */
        // Processes input first to determine if number label was called
        // More efficient number label processing? Skips iterating through array of numbers and assumes the array is numerical order if input is a Digit
        if (TextUtils.isDigitsOnly(word)) {
            if (Integer.parseInt(word) < currentTooltipCount){
                if (tooltipRequiredNodes.size() > Integer.parseInt(word) && tooltipRequiredNodes.get(Integer.parseInt(word)).nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    // perform click with condition to return once click is successful
                    Log.d(debugLogTag, "Clicked number: " + word);    // log the information
                    return true;
                }

            }
        }

        for(LabelFoundNode foundLabeledNode: foundLabeledNodes){
            if(foundLabeledNode.label.contains(word)){
                if (foundLabeledNode.nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                    Log.d(debugLogTag, "Clicked on description:" + word);
                    return true;
                }
  // return once clicked
            }
        }

        //Find ALL of the nodes that match the "text" argument.
        List<AccessibilityNodeInfo> list = currentSource.findAccessibilityNodeInfosByText(word);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                Log.d(debugLogTag, "Clicked on item:" + word);
            }
            return true;     // return once clicked
        }
        // for some element that named with first capital word
        String camelCaseWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        list = currentSource.findAccessibilityNodeInfosByText(camelCaseWord);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                Log.d(debugLogTag, "Clicked on item:" + word);
            }

            return true;     // return once clicked
        }


        return false;
    }
    public boolean commandExecution(String match){
        /**
         * This function will be called on user voice input as a string of command
         * @param: match : the user command interpreted into a string
         */
        String[] words = match.split(" ");
        ArrayList<String> trimmedWords = new ArrayList<String>();
        // ["open","uber"...]

        if(predefinedCommands.contains(match.toLowerCase())){
            String commands  = sharedPreferences.getString(match,null);
            currentSequence.clear();
            Collections.addAll(currentSequence, commands.split(";"));
            if(currentSequence.size() == 0){
                Log.d(debugLogTag,"no more item to press");
            }
            else if(commandExecution(currentSequence.get(0))){
                currentSequence.remove(0);
                Log.d(debugLogTag,"clicked based on saved actions, items left: " + currentSequence.size());
                isPlaying = true;
            }
            return true;
        }
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
        } else if (initialWord.equals("enter")) {
            String textToSet = match.substring(6);
            setTextForAllSubNode(currentSource,0,textToSet);
            isActionInvoked = true;
        }
        else if (initialWord.equals("scroll")){
            scrollingActivity(trimmedWords.get(1));
            isActionInvoked = true;
        } else if(!trimmedWords.get(0).equals("")){
            Log.d(debugLogTag,"clicking invoked for:" + trimmedWords.get(0));
            if (clickButtonByText(trimmedWords.get(0))) {
                isActionInvoked = true;
            }
        }
        if (isActionInvoked){
            if(isRecording){
                savedCommands.add(match);
            }
            AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (manager.isEnabled()) {
                AccessibilityEvent e = AccessibilityEvent.obtain();
                e.setEventType(AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED);
                e.setClassName(getClass().getName());
                e.getText().add("User interaction invoked this event");
                manager.sendAccessibilityEvent(e);
            }
        return true;
        }
        return false;
    }

    private void initializeSpeechRecognition() {
        /**
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
                        commandExecution(match);
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