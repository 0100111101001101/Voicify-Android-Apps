package com.research.voicify;

import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;

import android.net.Uri;

import android.os.Bundle;

import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.research.voicify.deeplink.DeepLinkList;

import java.util.List;



public class MainActivity extends AppCompatActivity {
    // NOTE: Initiate TextToSpeech variable
    TextToSpeech tts;
    // NOTE: UI View outlets below here
    Button settingBtn;
    Button deepLink;
    // NOTE: Global Variables below here
    List<String> argumentVector;
    int argumentCount = 0;
    SpeechRecognizer speechRecognizer;
    Intent speechRecognizerIntent;
    String debugLogTag = "FIT4003_VOICIFY";                  // use this tag for all log tags.

    // NOTE: Conditionals below here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Uri uri = getIntent().getData();
        if (uri != null) {
            argumentVector = uri.getPathSegments();
            argumentCount = argumentVector.size();          // deep linking example from WK1
        }

        // NOTE: Programmatically link UI elements here

        // NOTE: Set listeners to Views here
        deepLink = findViewById(R.id.deeplinkpage);
        deepLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, DeepLinkList.class);
                startActivity(myIntent);
            }
        });
        settingBtn = findViewById(R.id.buttonsetting);
        settingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(myIntent);
            }

        });

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