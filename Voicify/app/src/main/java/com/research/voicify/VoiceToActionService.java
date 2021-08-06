package com.research.voicify;

import android.accessibilityservice.AccessibilityService;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class VoiceToActionService extends AccessibilityService {

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
    public void printOutElementTree(AccessibilityNodeInfo source){
        for(int i = 0; i < source.getChildCount(); i++) {
            AccessibilityNodeInfo item = source.getChild(i);
            if (item == null) return;
            Log.d("Service Test", "First: " + item.getClassName() + " / ");

            for (int j = 0; j < item.getChildCount(); j++) {
                AccessibilityNodeInfo itemDeeper = item.getChild(j);
                if (itemDeeper == null) return;
                String classname1 = itemDeeper.getClassName().toString();
                Log.d("Service Test", "       Second: " + classname1 + " / " );
                for (int k = 0; k < itemDeeper.getChildCount(); k++) {
                    AccessibilityNodeInfo itemDeeper2 = itemDeeper.getChild(k);
                    if (itemDeeper2 == null) return;
                    String classname2 = itemDeeper2.getClassName().toString();
                    Log.d("Service Test", "             Third: " + classname2+ " / "  );

                    for (int l = 0; l < itemDeeper2.getChildCount(); l++) {
                        AccessibilityNodeInfo itemDeeper3 = itemDeeper2.getChild(l);
                        if (itemDeeper3 == null) return;
                        String classname3 = itemDeeper3.getClassName().toString();
                        Log.d("Service Test", "                     Fourth: " +classname3 + " / ");

                        for (int n = 0; n < itemDeeper3.getChildCount(); n++) {
                            AccessibilityNodeInfo itemDeeper4 = itemDeeper3.getChild(n);
                            if (itemDeeper4 == null) return;
                            String classname4  = itemDeeper4.getClassName().toString();
                            Log.d("Service Test", "                             Fifth: " + classname4 + " / " );


                            for (int m = 0; m < itemDeeper4.getChildCount(); m++) {
                                AccessibilityNodeInfo itemDeeper5 = itemDeeper4.getChild(m);
                                if (itemDeeper5 == null) return;
                                String classname5 = itemDeeper5.getClassName().toString();
                                Log.d("Service Test", "                                   Sixth: " + classname5 + " / ");

                            }
                        }
                    }
                }
            }
        }
    }
    @Override
    public void onInterrupt() {

        Log.d("Service Test","Service Disconnected");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        Log.d("Service Test","Service Connected");
    }
}