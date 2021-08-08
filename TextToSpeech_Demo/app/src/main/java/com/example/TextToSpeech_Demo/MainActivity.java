package com.example.TextToSpeech_Demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.speech.tts.TextToSpeech;
import android.widget.Switch;

import java.util.Locale;

public class MainActivity extends Activity {
    // Declaring variables
    TextToSpeech tts;
    Button sendButton;
    EditText textBox;
    Switch ttsToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve references to view elements on screen
        textBox = (EditText) findViewById(R.id.editTextReadoutField);
        sendButton = (Button) findViewById(R.id.button);
        ttsToggle = (Switch) findViewById(R.id.switch1);

        // Instantiate TextToSpeech object to main activity, this is where you can configure speaker voice options
        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    // Setting locale, speech rate and voice pitch
                    tts.setLanguage(Locale.UK);
                    tts.setSpeechRate(1.0f);
                    tts.setPitch(1.0f);

                }
            }

        });
        // Assigning listener to button to read textedit input, I prefer using the method option
//        sendButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String toSpeak = textBox.getText().toString();
//                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
//
//            }
//        });
    }

    public void onPause(){
        if(tts !=null){
            tts.stop();
            tts.shutdown();
        }
        super.onPause();
    }

    // Initiates TextToSpeech on content within EditText field when Button is clicked
    public void readMessage(View view) {
        textBox = (EditText) findViewById(R.id.editTextReadoutField);
        sendButton = (Button) findViewById(R.id.button);

        // Checks if master TextToSpeech Toggle is on
        if (ttsToggle.isChecked()) {
            String toSpeak = textBox.getText().toString();
            // Speaks string, QUEUE_FLUSH means all entries in playback queue are dropped and replaced  by the new entry
            tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);


            // TESTING stringReader Function REMOVE FROM ACTUAL IMPLEMENTATION

            // Calling generic stringReader function that can be used to speech text
            stringReader("Testing one two three");
            // Testing an empty/null string passed through
            stringReader("");
        }

    }

    // General Method that converts strings to speech
    public void stringReader(String message) {

        if (ttsToggle.isChecked()) {
            if (message == null || "".equals(message)) {
                tts.speak("I didn't catch that, could you please repeat that?", TextToSpeech.QUEUE_ADD, null);
            } else
                // QUEUE_ADD mode adds speech entry to the back of playback queue.
                tts.speak(message, TextToSpeech.QUEUE_ADD, null);
        }
    }

}