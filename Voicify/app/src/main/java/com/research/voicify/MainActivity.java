package com.research.voicify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ActivityNotFoundException;
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
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    // NOTE: Initiate TextToSpeech variable
    TextToSpeech tts;
    // NOTE: UI View outlets below here
    EditText speechTextOutlet;
    Button listenBtn;
    Switch ttsToggle;

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
        ttsToggle = (Switch) findViewById(R.id.ttsMaster);
        // NOTE: Set listeners to Views here

        listenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(debugLogTag, "start");
                speechRecognizer.startListening(speechRecognizerIntent);       // on click listener to start listening audio
            }

        });

    }

    // Tester functionality to start tts service and read out string entered in the edittext field
    public void ttsTester (View view){
        speakerTask(speechTextOutlet.getText().toString());
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

    // Use this method to call out to TTSService (Text-To-speech service) to speak out message
    public void speakerTask(String toSpeak) {
        if (ttsToggle.isChecked()) {
            Intent i = new Intent(this, TTSService.class);
            i.putExtra("message", toSpeak);
            // starts service for intent
            startService(i);
        }
    }


    // General Method that converts strings to speech (currently unused, see TTSService.java)
    public void stringReader(String message) {
        // If master toggle switch isn't enabled, TTS will not run
        if (ttsToggle.isChecked()) {

            if (message == null || "".equals(message)) {
                tts.speak("I didn't catch that, could you please repeat that?", TextToSpeech.QUEUE_FLUSH, null, null);
            } else
                // QUEUE_ADD mode adds speech entry to the back of playback queue.
                tts.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }
}

// Deep Link
//URL: app://www.voicifyApps.com/bigMac/burger/doordash
//URL: https://www.voicifyApps.com/bigMac/burger/doordash

// WIP: Check Asana
//
//    public static ArrayList<ArrayList<Integer>> launchTriggerIndices1 = new ArrayList<>();
//
//    public static ArrayList<ArrayList<Integer>> launchTriggerIndices2 = new ArrayList<>();
//
//    public static ArrayList<ArrayList<Integer>> launchTriggerIndices3 = new ArrayList<>();
//
//    public static String[] launchTriggers = new String[]{"open","load","start","launch","execute"};
//
//    public static String[] appNames = new String[]{"Play Store", "Google","Google Pay","Facebook","Youtube", "Music", "Amazon Store", "Alexa Play", "Music downloader", "Smart Launcher"};   // to emulate android api response
//
//    public static HashMap<String,String> appNameMap = new HashMap<String,String>();
//
//
//    public static void main(String[] args) {
//        // catches: google and play store are two different words & apps, open & start are both trigger words, "the" after a trigger statement, use of plural instead of singular, multiple trigger words.
//        // expected: Suggest Google or Play Store
//        String sentence1 = "Hey I can start you so can you please open the google play stores and after that maybe I can start working on the proposal";
//
//
//        // catches: start is a trigger statement and youtube is in the threshold but it shall not be opened, downloader has a trigger word load and google which is an app name is within the threshold, NLP may not be able to recognise "Music downloader" as an app but would rather return "Music" as a app, "the" after a trigger statement, Multiple app possibility, plural usage.
//        // expected: open Music downloader
//        String sentence2 = "I can start you but ignore youtube and google some stuff after that open the music downloaders";
//
//        // catches: launch is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word.
//        // expected: launch smart launcher
//        String sentence3 = "Hey I like android development so can you please launch smart launcher we google about the proposal";
//
//        sentence1 = sentence1.toLowerCase().trim();
//        sentence2 = sentence2.toLowerCase().trim();
//        sentence3 = sentence3.toLowerCase().trim();
//
//        String charSequence1 = sentence1.replace(" ", "");
//        String charSequence2 = sentence2.replace(" ", "");
//        String charSequence3 = sentence3.replace(" ", "");
//
//        // func calls
//
//        launchTriggerIndices1 = getLaunchTriggerIndices(sentence1);
//        launchTriggerIndices2 = getLaunchTriggerIndices(sentence2);
//        launchTriggerIndices3 = getLaunchTriggerIndices(sentence3);
//
//        appNameMap = appNameCharSequenceMapper(appNames);
//
//        System.out.println("\n\nApp name & their char sequences: ");
//        System.out.println(appNameMap);
//
//        System.out.println("\n\nLaunch Trigger Word Indexes: ");
//        System.out.println(launchTriggerIndices1);
//        System.out.println(launchTriggerIndices2);
//        System.out.println(launchTriggerIndices3);
//
//        System.out.println("\n\nSearchable char sequences: ");
//        System.out.println(getSearchableCharSequence(sentence1,launchTriggerIndices1));
//        System.out.println(getSearchableCharSequence(sentence2,launchTriggerIndices2));
//        System.out.println(getSearchableCharSequence(sentence3,launchTriggerIndices3));
//
//
//    }
//
//    private static ArrayList<String> getSearchableCharSequence(String sentence, ArrayList<ArrayList<Integer>> triggerIndices){
//
//        ArrayList<String> results = new ArrayList<>();
//        int startIndex=0, endIndex = charSequence.length();
//
//        if(triggerIndices.size() > 1) {
//            for(ArrayList<Integer> matchIndex : triggerIndices){
//
//            }
//        } else if (triggerIndices.size() == 1){
//            results.add(charSequence.substring(triggerIndices.));
//        }
//
//        return results;
//    }
//
//    // private static ArrayList<Integer> getLaunchTriggers(String[] words) {
//    //   ArrayList<Integer> launchTriggerIndices = new ArrayList<>();
//    //   for(int wordIndex = 0; wordIndex < words.length; wordIndex++) {
//    //     String word = words[wordIndex];
//    //     for (String trigger : launchTriggers){
//    //       if (word.equals(trigger)){
//    //           launchTriggerIndices.add(wordIndex);
//    //           break;
//    //       }
//    //     }
//    //   }
//    //   return launchTriggerIndices;
//    // }
//
//    private static ArrayList<ArrayList<Integer>> getLaunchTriggerIndices(String sentence){
//        ArrayList<ArrayList<Integer>> launchTriggerIndices = new ArrayList<>();
//        int spaceCount = 0;
//        String word = "";
//        int startIndex = 0;
//        for(int charIndex = 0; charIndex < sentence.length(); charIndex++){
//            if(sentence.charAt(charIndex) == ' ') {
//                for (String trigger : launchTriggers){
//                    if (word.equals(trigger)){
//                        ArrayList<Integer> matchIndex = new ArrayList<>();
//                        matchIndex.add(startIndex);
//                        matchIndex.add(charIndex);
//                        launchTriggerIndices.add(matchIndex);
//                        break;
//                    }
//                }
//                spaceCount+=1;
//                word = "";
//                startIndex = 0;
//                continue;
//            } else if (startIndex == 0){
//                startIndex = charIndex;
//            }
//            word += sentence.charAt(charIndex);
//        }
//        return launchTriggerIndices;
//    }
//
//    private
//
//    private static HashMap<String,String> appNameCharSequenceMapper(String[] appNames){
//        HashMap<String,String> appNameCharSequenceMap = new HashMap<String,String>();
//
//        for (String appName : appNames) {
//            String appNameCharSeq = appName.toLowerCase().trim().replace(" ", "");
//            appNameCharSequenceMap.put(appNameCharSeq,appName);
//        }
//        return appNameCharSequenceMap;
//    }
//
//// private static ArrayList<ArrayList<Integer>> getLaunchTriggers(String charSequence) {
////   ArrayList<ArrayList<Integer>> launchTriggerIndices = new ArrayList<>();
////   boolean flushWord;
////   for(int charIndex=0; charIndex<charSequence.length(); charIndex++) {
////     flushWord = false;
////     String potentialWord = charSequence.substring(charIndex,charIndex+1);
////     for(int multiCharIndex = charIndex+1; multiCharIndex<charSequence.length(); multiCharIndex++){
////       potentialWord+=charSequence.charAt(multiCharIndex);
////         for(String launchTrigger : launchTriggers){
////           if (potentialWord.equals(launchTrigger)) {
////             ArrayList<Integer> matchIndex = new ArrayList<>();
////             matchIndex.add(charIndex);
////             matchIndex.add(multiCharIndex+1);
////             flushWord = true;
////             launchTriggerIndices.add(matchIndex);
////             break;
////           }
////       }
////       if (flushWord)
////           break;
////     }
////   }
////   return launchTriggerIndices;
//// }

//--------------------------------------------------------------------------------------------------