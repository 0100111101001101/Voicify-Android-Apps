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
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.research.voicify.CustomActionTargetMatching.ActionTargetMatcher;
import com.research.voicify.GoogleNLU.GoogleAutoML;
import com.research.voicify.GoogleNLU.RequestBody;
import com.research.voicify.GoogleNLU.RespondBody;
import com.research.voicify.elements.LabelFoundNode;
import com.research.voicify.elements.TooltipRequiredNode;

import java.io.IOException;
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

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * @author: Om Harish Mandavia (oman0003, 29145643), Minh Duc Vu (mvuu0003, 29068215), Alex Dumitru(adum6, 27820289)
 * @version: V1.8
 * @implNote: last updated on 09/10/2021
 */


@RequiresApi(api = Build.VERSION_CODES.R)
public class VoiceToActionService extends AccessibilityService implements View.OnTouchListener{
    final String FILE_NAME = "voicify";
    final String ALL_COMMANDS = "all_commands";
    int width,height;

    ArrayList<String> uiElements = new ArrayList<String>();
    ArrayList<String> previousUIElements = new ArrayList<String>();
    ArrayList<String> appNames = new ArrayList<String>();

    SharedPreferences sharedPreferences;
    ArrayList<String> predefinedCommands = new ArrayList<>();
    ArrayList<String> currentSequence = new ArrayList<String>();
    AccessibilityNodeInfo currentSource;
    AccessibilityNodeInfo previousSource;
    ArrayList<AccessibilityNodeInfo> scrollableNodes = new ArrayList<AccessibilityNodeInfo>();
    FrameLayout mLayout;
    ArrayList<LabelFoundNode> foundLabeledNodes = new ArrayList<>();
    ArrayList<TooltipRequiredNode> tooltipRequiredNodes = new ArrayList<>();
    int noOfLabels = 0;
    boolean isOn = false;
    boolean hasExecuted = true;
    boolean skipPreviousRTECheck = false;
    SpeechRecognizer speechRecognizer;                      // declaring speech recognition var
    Intent speechRecognizerIntent;
    String debugLogTag = "FIT4003_VOICIFY";                  // use this tag for all log tags.
    ArrayList<String> launchTriggers = new ArrayList<String>(Arrays.asList("load","start","launch","execute","open"));
    String[] pressTriggers = new String[]{"press","click"};

    // Defining window manager for overlay elements and switchBar
    WindowManager wm;
    WindowManager.LayoutParams switchBar; // stores layout parameters for movable switchBar


    String[] tooltipColorSpinnerItems = new String[]{"#64b5f6","#2b2b2b","#ff4040"};
    int[] tooltipSizeSpinnerItems = new int[]{14,18,22};
    int[] tooltipOpacitySpinnerItems = new int[]{250,220,170,120};
    int[] buttonOpacitySpinnerItems = new int[]{250,220,170,120};
    boolean[] buttonRecordItems = new boolean[]{false,true};
    boolean[] buttonAlgoItems = new boolean[]{true,false};

    int tooltipColor = 0 ;
    int tooltipSize = 0;
    int tooltipOpacity = 0;
    int buttonOpacity = 0;
    int buttonRecordTxt = 0;
    int buttonAlgoTxt = 0;
    long currentTime;

    // variable for switch bar coordinates
    private int initialX;
    private int initialY;
    private float initialTouchX;
    private float initialTouchY;

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


        if (source == null && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return;
        }
        if(previousSource!= null &&  source != null && (previousSource.hashCode() == source.hashCode() || previousSource.equals(source))){
            Log.d(debugLogTag, "Blocked event");
            return;
        }
        previousSource = source;
        if(isNotBlockedEvent()) {
            checkSettingsChanged();
            uiElements.clear();
            noOfLabels = 0;
            removeAllTooltips();    // remove all old  tooltip when screen changed
            currentSource = getRootInActiveWindow(); // update the current root node
            if(isOn) {
                printOutAllClickableElement(getRootInActiveWindow(), 0, event); // call function for root node
            }
            this.executeCommand("");
        }
    }

    public void checkSettingsChanged(){
        tooltipColor = sharedPreferences.getInt(SettingsActivity.TOOLTIP_COLOR,0);
        tooltipSize = sharedPreferences.getInt(SettingsActivity.TOOLTIP_SIZE,0);
        tooltipOpacity = sharedPreferences.getInt(SettingsActivity.TOOLTIP_OPACITY,0);
        int previousBtnOpacity = buttonOpacity;
        int previousBtnRecord = buttonRecordTxt;
        int previousBtnAlgo = buttonAlgoTxt;
        buttonOpacity = sharedPreferences.getInt(SettingsActivity.BUTTON_OPACITY,0);
        buttonRecordTxt = sharedPreferences.getInt(SettingsActivity.BUTTON_RECORD,0);
        buttonAlgoTxt = sharedPreferences.getInt(SettingsActivity.BUTTON_ALGO,0);
        if(previousBtnOpacity != buttonOpacity ||buttonRecordTxt != previousBtnRecord){
            wm.removeView(mLayout);
            createSwitch();
        }
    }
    final Handler autoReloadHandler = new Handler();
    final Runnable runnable = new Runnable() {
        public void run() {
            autoReload();
            autoReloadHandler.postDelayed(this, 3000);

        }
    };
    public boolean isNotBlockedEvent(){
        Date date = new Date();
        long time = date.getTime();
        if (time - currentTime > 750){
            currentTime = time;
            return true;
        }
        //Log.d(debugLogTag, "Event blocked for repetitive calls");
        return false;
    }

    public void autoReload(){
            AccessibilityManager manager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
            if (manager.isEnabled()) {
                AccessibilityEvent e = AccessibilityEvent.obtain(AccessibilityEvent.TYPE_ANNOUNCEMENT);
                manager.sendAccessibilityEvent(e);
         //       Log.d(debugLogTag, "Auto Reloaded");
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
            Rect rectTest = new Rect();                     //  to get the coordinate of the UI element
            nodeInfo.getBoundsInScreen(rectTest);           //  store data of the node
            if(rectTest.right -100 < width && rectTest.bottom-100<height && rectTest.left+100 > 0 && rectTest.top+100 >0){
                if (nodeInfo.getText() != null) {   // check if node has a corresponding text
                    label += nodeInfo.getText();
                     //Log.d(debugLogTag,"Available commands: " + label);
                    uiElements.add(label);
                    return;
                } else {
                    // no information about node or event (Tags to be assigned!)
                    String foundLabel  = searchForTextView(nodeInfo,"");
                    if (!foundLabel.equals("") && noOfLabels < 10){
                        foundLabeledNodes.add(new LabelFoundNode(nodeInfo,foundLabel.toLowerCase()));
                        //Log.d(debugLogTag,"Available commands: " +foundLabel.toLowerCase());
                        uiElements.add(foundLabel.toLowerCase());
                        noOfLabels+=1;
                    } else if (currentTooltipCount < 10){
                            //Log.d(debugLogTag, currentTooltipCount+ ": Left " + rectTest.left + " Top " + rectTest.top+ " Right " + rectTest.right + " Bottom " + rectTest.bottom);
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

        if( currentNode.getClassName() != null && currentNode.getClassName().equals("android.widget.TextView") && currentNode.getText() != null){
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
        Log.d(debugLogTag, "filling " + text);
        if (nodeInfo == null) return;   // null check
        if(nodeInfo.isEditable()){      // check if the node has editable field
            setGivenText(nodeInfo,text);       // call a method to put in the text
        }
        for (int i = 0; i < nodeInfo.getChildCount(); ++i) {    // recursive call to reach all nested nodes/leaves
            setTextForAllSubNode(nodeInfo.getChild(i), depth + 1, text);
        }
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
                command = command.toLowerCase().trim();

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
                        //Log.d(debugLogTag,"Gesture Completed");
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
        loadPresavedCommands();
        getDisplayMetrics();
        Log.d(debugLogTag,"Service Connected");
        loadAppNames();
        autoReloadHandler.post(runnable);
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

        // Check for permissions
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        }
        mLayout = new FrameLayout(this);

        // Create layout for switchBar
        switchBar = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        switchBar.gravity = Gravity.TOP;  // stick it to the top
        //WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |


        LayoutInflater inflater = LayoutInflater.from(this);
        View actionBar = inflater.inflate(R.layout.action_bar, mLayout);
        wm.addView(mLayout, switchBar);       // add it to the screen

        //trying to get this work for all 3 buttons
        View[] buttonArray = {(View)actionBar.findViewById(R.id.listenBtn), (View)actionBar.findViewById(R.id.recordBtn)};

        //set an ontouchlistener for each button
        for (int i=0; i < buttonArray.length; i++){
            buttonArray[i].setOnTouchListener(this);

        }


        Button recordBtn = mLayout.findViewById(R.id.recordBtn);
        Button listenBtn = mLayout.findViewById(R.id.listenBtn);

        Drawable unwrappedDrawable = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.roundedbutton);
        Drawable wrappedDrawable = DrawableCompat.wrap(unwrappedDrawable);
        DrawableCompat.setTint(wrappedDrawable, Color.argb(buttonOpacitySpinnerItems[buttonOpacity],255,255,255));

        recordBtn.setBackgroundResource(R.drawable.roundedbutton);
        listenBtn.setBackgroundResource(R.drawable.roundedbutton);

        configureListenButton(listenBtn);
        configureRecordButton(recordBtn);

        if(!buttonRecordItems[buttonRecordTxt]){
            recordBtn.setVisibility(View.GONE);
        }

    }

    private void inflateTooltip(int x, int y, AccessibilityNodeInfo nodeInfo){
        /**
         * This function will configure each of the tooltip on the screen, so this function will be
         * called for each of the tooltip on the screen.
         * param: x is the location in x axis
         * param: y is the location in y axis
         */
        FrameLayout tooltipLayout = new FrameLayout(this);      // create new layout for each tooltip
        WindowManager.LayoutParams tooltipLayoutParams = new WindowManager.LayoutParams();
        tooltipLayoutParams.type = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;
        tooltipLayoutParams.format = PixelFormat.TRANSLUCENT;
        tooltipLayoutParams.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        tooltipLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        tooltipLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        tooltipLayoutParams.gravity = Gravity.TOP|Gravity.START;     // reset the (0,0) to the top left screen
        tooltipLayoutParams.x = x;       // x location
        tooltipLayoutParams.y = y - 100;       // y location
        LayoutInflater inflater = LayoutInflater.from(this);
        inflater.inflate(R.layout.tooltip_number, tooltipLayout);   // inflate the view to the screen
        wm.addView(tooltipLayout, tooltipLayoutParams);

        TextView tooltip = tooltipLayout.findViewById(R.id.tooltip);    // set the count based on current count
        tooltip.setText(currentTooltipCount + "");
        tooltip.setTextSize(tooltipSizeSpinnerItems[tooltipSize]);
        tooltip.setBackgroundResource(R.drawable.tooltip_shape);  //drawable id
        GradientDrawable gd = (GradientDrawable) tooltip.getBackground().getCurrent();
        gd.setColor(Color.parseColor(tooltipColorSpinnerItems[tooltipColor])); //set color
        gd.setAlpha(tooltipOpacitySpinnerItems[tooltipOpacity]);        // add to the list to retrieve later
        gd.setSize(tooltipSizeSpinnerItems[tooltipSize] + 40,tooltipSizeSpinnerItems[tooltipSize]+ 5);
        tooltipRequiredNodes.add(new TooltipRequiredNode(nodeInfo,currentTooltipCount,tooltipLayout));
        uiElements.add(Integer.toString(currentTooltipCount));
        currentTooltipCount += 1;

    }

    // This method is responsible for updating the switchBar coordniates upon touch and updating the view
    @Override
    public boolean onTouch(View view1, MotionEvent motionEvent){

        switch(motionEvent.getAction()){

            case MotionEvent.ACTION_DOWN:
                initialX = switchBar.x;
                initialY = switchBar.y;
                initialTouchX = motionEvent.getRawX();
                initialTouchY = motionEvent.getRawY();
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                switchBar.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                switchBar.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                wm.updateViewLayout(mLayout, switchBar);
                break;
        }
        return false;
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

    private void configureListenButton(Button listenBtn) {
        /**
         * This function is called after the service has been connected. This function binds
         * functionality to the master button which can be used to turn on/off the tool.
         *
         * @param: None
         * @return: None
         * @post-cond: functionality has been added to the inflated button
         * */


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
    private void configureRecordButton(Button listenBtn) {

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

                    //Log.d(debugLogTag," " + savedCommands.size());
                    //Log.d(debugLogTag," " + savedCommands);
                }
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
                if(appName.equals(inputName)){
                    Intent mIntent = getPackageManager().getLaunchIntentForPackage(
                            packageInfo.packageName);
                    if (mIntent != null) {
                        try{
                            startActivity(mIntent);
                            // Adding some text-to-speech feedback for opening apps based on input
                            // Text-to-speech feedback if app not found);
//                            speakerTask(speechPrompt.get("open") + inputName);

                        } catch (ActivityNotFoundException err) {
                            // Text-to-speech feedback if app not found
//                            speakerTask(speechPrompt.get("noMatch") + inputName);

                            // Render toast message on screen
                            Toast t = Toast.makeText(getApplicationContext(),
                                    "APP NOT FOUND", Toast.LENGTH_SHORT);
                            t.show();
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();                // handling app not found exception
//                speakerTask(speechPrompt.get("noMatch") + inputName);
            }
        }
    }

    private void loadAppNames() {
        final PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA); // getting meta data of all installed apps

        for (ApplicationInfo packageInfo : packages) {          // checking if the input has a match with app name
            try {
                ApplicationInfo info = pm.getApplicationInfo(packageInfo.packageName, PackageManager.GET_META_DATA);
                String appName = (String) pm.getApplicationLabel(info).toString().toLowerCase();
                this.appNames.add(appName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();                // handling app not found exception
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

    boolean clickButtonByText(String word) {
        /**
         * This function will click a button (anything thats clickable) with provided information
         * param: word: a string to store data about what to click
         */
        // Processes input first to determine if number label was called
        // More efficient number label processing? Skips iterating through array of numbers and assumes the array is numerical order if input is a Digit
        if (TextUtils.isDigitsOnly(word)) {
            //Log.d(debugLogTag,word);
            if (Integer.parseInt(word) <= currentTooltipCount){
                if (tooltipRequiredNodes.size() >= Integer.parseInt(word) && tooltipRequiredNodes.get(Integer.parseInt(word)-1).nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                    // perform click with condition to return once click is successful
                    //Log.d(debugLogTag, "Clicked number: " + word);    // log the information
                    return true;
                }

            }
        }

        for(LabelFoundNode foundLabeledNode: foundLabeledNodes){
            if(foundLabeledNode.label.contains(word)){
                if (foundLabeledNode.nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                    //Log.d(debugLogTag, "Clicked on description:" + word);
                    return true;
                }
  // return once clicked
            }
        }

        if (currentSource == null) {return false;}

        //Find ALL of the nodes that match the "text" argument.
        List<AccessibilityNodeInfo> list = currentSource.findAccessibilityNodeInfosByText(word);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                //Log.d(debugLogTag, "Clicked on item:" + word);
            }
            return true;     // return once clicked
        }
        // for some element that named with first capital word
        String camelCaseWord = word.substring(0, 1).toUpperCase() + word.substring(1);
        list = currentSource.findAccessibilityNodeInfosByText(camelCaseWord);    // find the node by text
        for (final AccessibilityNodeInfo node : list) { // go through each node to see if action can be performed
            if (node.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
                //Log.d(debugLogTag, "Clicked on item:" + word);
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
        //Log.d(debugLogTag,match);


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
            openApp(match);
        } else if (initialWord.equals("enter")) {
            String textToSet = match.substring(6);
            setTextForAllSubNode(currentSource,0,textToSet);
            isActionInvoked = true;
        }
        else if (initialWord.equals("scroll")){
            scrollingActivity(trimmedWords.get(1));
            isActionInvoked = true;
        } else if(!trimmedWords.get(0).equals("")){
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

    private void executeCommand(String sentence){
        String currentCommand = "";
        if(!hasExecuted) {
            return;
        }

        if(predefinedCommands.contains(sentence.toLowerCase().trim())){

            String commands  = sharedPreferences.getString(sentence.toLowerCase().trim(),null);
            sentence = commands.replaceAll(";"," and ");
            Log.d(debugLogTag, "Loaded predefined commands: "+ sentence);
        }

        Log.d(debugLogTag, "Current EXEC SEQ: " + currentSequence.toString());

        if(sentence.equals("") && currentSequence.size() == 0) {
            skipPreviousRTECheck = true;
            return;
        }

        if(!sentence.equals("")){
            skipPreviousRTECheck = true;
            Collections.addAll(currentSequence, sentence.split("and"));
            Log.d(debugLogTag, "New EXEC SEQ: " + currentSequence.toString());
        }

        if(!skipPreviousRTECheck){
            if(uiElements.equals(previousUIElements)) {
                Log.d(debugLogTag, "No change in RTE: " + uiElements.toString() + ", " + previousUIElements.toString());
                return;
            }
        }

        if(currentSequence.size() != 0) {
            ActionTargetMatcher currentCommandMatcher;
            currentCommand = currentSequence.remove(0);
            if (currentCommand.contains("back")){
                performGlobalAction(GLOBAL_ACTION_BACK);

            } else if (currentCommand.contains("home")) {
                performGlobalAction(GLOBAL_ACTION_HOME);
            } else if (currentCommand.replace(" ","").contains("shutdownservice")){
                Button listenBtn = mLayout.findViewById(R.id.listenBtn);
                listenBtn.setText("Start");
                isOn = false;
                speechRecognizer.stopListening();           // on click listener to stop listening & processing data
            } else {
                currentCommandMatcher = new ActionTargetMatcher(currentCommand, uiElements, appNames);
                previousUIElements.clear();
                previousUIElements.addAll(uiElements);
                skipPreviousRTECheck = false;
//                Log.d(debugLogTag, "Executing: " + currentCommand + " RTEnv: " + uiElements.toString());
                ArrayList<HashMap<ActionTargetMatcher.Action, ArrayList<String>>> actionTargetPairs = currentCommandMatcher.getCommandActionTargets();
                Log.d(debugLogTag, "AT Pairs: " + actionTargetPairs.toString());

                if (actionTargetPairs.size() == 0 && !sentence.contains("match not found") && !sentence.contains("did you mean")) {
                    skipPreviousRTECheck = true;
                    this.speakerTask("Target match not found. ");
                    //this.executeCommand("");
                    currentSequence.clear();
                }
                for (HashMap<ActionTargetMatcher.Action, ArrayList<String>> actionTargetPair : actionTargetPairs) {
                    //Log.d(debugLogTag, actionTargetPair.toString());
                    List<ActionTargetMatcher.Action> actionList = new ArrayList<ActionTargetMatcher.Action>(actionTargetPair.keySet());
                    ActionTargetMatcher.Action currentAction = actionList.get(0);

                    if(actionTargetPair.get(currentAction).size() > 1){
                        this.speakerTask("Exact match not found. Did you mean "+ actionTargetPair.get(currentAction).get(0) + " or " +actionTargetPair.get(currentAction).get(1) + "?");
                        skipPreviousRTECheck = true;
                        //this.executeCommand("");
                        currentSequence.clear();
                        return;
                    }

                    hasExecuted = false;
                    boolean isExecuted = false;
                    if (currentAction == ActionTargetMatcher.Action.CLICK) {
                        clickButtonByText(actionTargetPair.get(currentAction).get(0));
                        isExecuted = true;
                    } else if (currentAction == ActionTargetMatcher.Action.SCROLL) {
                        scrollingActivity(actionTargetPair.get(currentAction).get(0));
                        skipPreviousRTECheck = true;
                        isExecuted = true;
                    } else if (currentAction == ActionTargetMatcher.Action.OPEN) {
                        openApp(actionTargetPair.get(currentAction).get(0));
                        isExecuted = true;
                    } else if (currentAction == ActionTargetMatcher.Action.ENTER) {
                        setTextForAllSubNode(currentSource, 0, actionTargetPair.get(currentAction).get(0));
                        skipPreviousRTECheck = true;
                        isExecuted = true;
                    }
                    if(isRecording && isExecuted){
                        Log.d(debugLogTag,"Duc" + currentCommand);
                        savedCommands.add(currentCommand);
                    }
                }
            }
            hasExecuted = true;
            try{
                java.util.concurrent.TimeUnit.SECONDS.sleep(3); // emulates thread synchronisation
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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
        // setting the limit for the service to listen as an requirement for API30
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100000);

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
                    if(isOn) {
                        speechRecognizer.startListening(speechRecognizerIntent);
                    }
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
                        Log.d(debugLogTag,match);
                        if(buttonAlgoItems[buttonAlgoTxt]){
                            Log.d(debugLogTag,"Using custom Algo");
                            executeCommand(match);
                        } else {
                            Log.d(debugLogTag,"Using  AutoML");
                            autoMLRequest(match);
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

    // Enter action
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


    private void autoMLRequest(String command){
        String token = "ya29.c.Kp8BFAjn21ijznPpm2s_MSYIWVxtw-RG8GtPGivsG18brZD9YeLCPgYAuKgncT5I--OH64tpT7Bky1SmcxP9q8v-wK3kiAgdymR9X7spw6LO1Lftw_xjdjCqF0YAhWEl8JJpoI2LNnlMtMbg2somJJrMgNQkHWZgAicThtGzYDfLhlOnKLostXTm3OO8L4ABLjk995FTPrUBptMdjn5NGk01";
        Interceptor headerInterceptor  = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                return chain.proceed(chain.request().newBuilder()
                        .addHeader("Authorization","Bearer " + token).build());

            }
        };

        RequestBody.Payload.TextSnippet textSnippet = new RequestBody.Payload.TextSnippet(command);
        RequestBody.Payload payload = new RequestBody.Payload(textSnippet);
        RequestBody requestBody = new RequestBody(payload);

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        //clientBuilder.addInterceptor(loggingInterceptor);
        clientBuilder.addInterceptor(headerInterceptor);
        Retrofit retrofit = new Retrofit.Builder()  // retrofit boilerplate
                .baseUrl("https://automl.googleapis.com/v1/")
                .client(clientBuilder.build())
                .addConverterFactory(GsonConverterFactory.create()) // add converter
                .build();


        GoogleAutoML service = retrofit.create(GoogleAutoML.class);
        Call<RespondBody> serviceSynonyms = service.getEntities(requestBody);  // pass in input string to find synonyms.
        serviceSynonyms.enqueue(new Callback<RespondBody>(){  // asynchronous call

            @Override
            public void onResponse(Call<RespondBody> call, Response<RespondBody> response) {
                String output = "";
                RespondBody respondBody = response.body();
                RespondBody.Payload[] payloads = respondBody.getPayload();
                boolean isAction = false;
                String action = "";
                for(RespondBody.Payload payload1: payloads){
                    if(!isAction){
                        if (payload1.getDisplayName().equals("Action")){
                            action = payload1.textExtraction.textSegment.getContent();

                            isAction = true;
                        }
                    } else if (!action.equals("")){
                        output += " and " + action + " " +payload1.textExtraction.textSegment.getContent();
                        action = "";
                        isAction = false;
                        }
                }
                if(output.length() > 0) {
                    Log.d(debugLogTag,output.substring(4));
                    executeCommand(output.substring(4));
                } else {
                    executeCommand(command);
                }

            }
            @Override
            public void onFailure(Call<RespondBody> call, Throwable t) {
                Log.d(debugLogTag, t.getMessage() + " Error");

            }
        });
    }
}


/*
* 1) Button for switching between AutoML & Custom Action-Target Matching algorithm
* 2) Function in VoiceToActionService to execute action on a target.
*       param - ArrayList<HashMap<Action,String>>
                eg: [{OPEN: "chrome"}]
  3)
* */