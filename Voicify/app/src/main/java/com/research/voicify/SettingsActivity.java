package com.research.voicify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class SettingsActivity extends AppCompatActivity   implements
        AdapterView.OnItemSelectedListener{

    String tooltipColor = "";
    String tooltipOpacity = "";
    String tooltipSize = "";
    String buttonOpacity = "";
    String buttonRecordTxt = "";

    String[] tooltipColorSpinnerItems = new String[]{"black","red","blue"};
    String[] tooltipOpacitySpinnerItems = new String[]{"25%","50%", "75%","100%"};
    String[] tooltipSizeSpinnerItems = new String[]{"small","medium", "large"};
    String[] buttonOpacitySpinnerItems = new String[]{"small","medium", "large"};
    String[] buttonRecordItems = new String[]{"show","hide"};


    SharedPreferences sharedPreferences;
    final String FILE_NAME = "voicify";
    final String BUTTON_OPACITY = "btn_opacity";
    final String BUTTON_RECORD = "btn_record";
    final String TOOLTIP_COLOR = "tooltip_color";
    final String TOOLTIP_SIZE = "tooltip_size";
    final String TOOLTIP_OPACITY = "tooltip_opacity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        sharedPreferences = getSharedPreferences(FILE_NAME,0);

        tooltipColor = sharedPreferences.getString(TOOLTIP_COLOR,tooltipColorSpinnerItems[0]);
        tooltipOpacity = sharedPreferences.getString(TOOLTIP_OPACITY,tooltipOpacitySpinnerItems[0]);
        tooltipSize = sharedPreferences.getString(TOOLTIP_SIZE,tooltipSizeSpinnerItems[0]);
        buttonOpacity = sharedPreferences.getString(BUTTON_OPACITY,buttonOpacitySpinnerItems[0]);
        buttonRecordTxt = sharedPreferences.getString(BUTTON_RECORD,buttonRecordItems[0]);

        SharedPreferences.Editor editor = sharedPreferences.edit();         // call an editor to modify SF

        Spinner tooltipColorSpinner = findViewById(R.id.tooltip_color);
          ArrayAdapter<String> tooltipColorAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipColorSpinnerItems);
        tooltipColorSpinner.setAdapter(tooltipColorAdapter);
        tooltipColorSpinner.setOnItemSelectedListener(this);

        Spinner tooltipOpacitySpinner = findViewById(R.id.tooltip_opacity);
        ArrayAdapter<String> tooltipOpacityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipOpacitySpinnerItems);
        tooltipOpacitySpinner.setAdapter(tooltipOpacityAdapter);
        tooltipOpacitySpinner.setOnItemSelectedListener(this);

        Spinner tooltipSizeSpinner = findViewById(R.id.tooltip_size);
        ArrayAdapter<String> tooltipSizeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tooltipSizeSpinnerItems);
        tooltipSizeSpinner.setAdapter(tooltipSizeAdapter);
        tooltipSizeSpinner.setOnItemSelectedListener(this);

        Spinner buttonOpacitySpinner = findViewById(R.id.button_opacity);
        ArrayAdapter<String> buttonOpacityAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, buttonOpacitySpinnerItems);
        buttonOpacitySpinner.setAdapter(buttonOpacityAdapter);
        buttonOpacitySpinner.setOnItemSelectedListener(this);

        Spinner buttonRecord = findViewById(R.id.button_record);
        ArrayAdapter<String> buttonRecordAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, buttonRecordItems);
        buttonRecord.setAdapter(buttonRecordAdapter);
        buttonRecord.setOnItemSelectedListener(this);

        Button saveBtn = findViewById(R.id.button3);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString(BUTTON_OPACITY,buttonOpacity);
                editor.putString(BUTTON_RECORD,buttonRecordTxt);
                editor.putString(TOOLTIP_COLOR,tooltipColor);
                editor.putString(TOOLTIP_SIZE,tooltipSize);
                editor.putString(TOOLTIP_OPACITY,tooltipOpacity);
                editor.apply();
                Intent myIntent = new Intent(SettingsActivity.this, MainActivity.class);
                startActivity(myIntent);
            }
        });



        }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.tooltip_color)
        {
            tooltipColor = tooltipColorSpinnerItems[position];
        }
        else if(spinner.getId() == R.id.tooltip_opacity)
        {
            tooltipOpacity =  tooltipOpacitySpinnerItems[position];
        }
        else if(spinner.getId() == R.id.tooltip_size)
        {
            tooltipSize =  tooltipSizeSpinnerItems[position];
        }
        else if(spinner.getId() == R.id.button_opacity)
        {
            buttonOpacity =  buttonOpacitySpinnerItems[position];
        }
        else if(spinner.getId() == R.id.button_record)
        {
            buttonRecordTxt =  buttonRecordItems[position];
        }

    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if(spinner.getId() == R.id.tooltip_color)
        {
            tooltipColor = tooltipColorSpinnerItems[0];
        }
        else if(spinner.getId() == R.id.tooltip_opacity)
        {
            tooltipOpacity =  tooltipOpacitySpinnerItems[0];
        }
        else if(spinner.getId() == R.id.tooltip_size)
        {
            tooltipSize =  tooltipSizeSpinnerItems[0];
        }
        else if(spinner.getId() == R.id.button_opacity)
        {
            buttonOpacity =  buttonOpacitySpinnerItems[0];
        }
        else if(spinner.getId() == R.id.button_record)
        {
            buttonRecordTxt =  buttonRecordItems[0];
        }
    }
}
