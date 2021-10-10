package com.research.voicify.CustomActionTargetMatching;
import android.util.Log;

import java.util.*;

public class ActionTargetMatcher {
    public enum Action {
        CLICK,    // APP NAMES + UI Elements
        SCROLL,   // UP, DOWN, LEFT, RIGHT
        OPEN,     // APP NAMES
        ENTER     // text until (end,conjunction,action)
    }

    enum ScrollTarget {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    public static HashMap<Action,ArrayList<String>> actionSynonyms = new HashMap<Action,ArrayList<String>>();
    public static ArrayList<HashMap<Action, String>> actionTargetRangeList = new ArrayList<HashMap<Action, String>> ();
    public static ArrayList<String> appNames, uiElements;
    String sentence = "";

    public ActionTargetMatcher(String sentence, ArrayList<String> uiElements, ArrayList<String> appNames) {

        this.sentence = sentence;
        this.uiElements = uiElements;
        this.appNames = appNames;

        // STEP 1: Check synonyms, if absent, fetching synonyms from REST API & caching it
        fetchSynonyms();

        // STEP 2: Cleaning all the possible UI Targets () and Commands for the algorithm
        sentence = this.cleanElements(sentence);

        getActionTriggers(sentence);
    }

    /*
    This method is used to mock an API Call from Sysnonyms REST API.

    @post-cond: adds all the synonyms for actions enum in the global variable
                (Key: Action enum, Value: Synonyms of action)
    */
    private void fetchSynonyms(){
        ArrayList<String> clickSynonyms = new ArrayList<String>(Arrays.asList("tap","touch","press"));
        ArrayList<String> scrollSynonyms = new ArrayList<String>(Arrays.asList("swipe","move","go"));
        ArrayList<String> openSynonyms = new ArrayList<String>(Arrays.asList("launch","start","execute"));
        ArrayList<String> enterSynonyms = new ArrayList<String>(Arrays.asList("fill","write","type"));
        actionSynonyms.put(Action.CLICK, clickSynonyms);
        actionSynonyms.put(Action.SCROLL, scrollSynonyms);
        actionSynonyms.put(Action.OPEN, openSynonyms);
        actionSynonyms.put(Action.ENTER, enterSynonyms);
    }

    /*
    This method is used to clean the given string. It removes all the characters that are not possible to be produced by
    speech to text recognition listener using regex. It removes all the articles from the string.

    @param: This is the string that will be used to handle symbols and articles.
    @return: It returns a string that is cleaned.
    */
    private String cleanElements(String command){
        ArrayList<String> articles = new ArrayList<String>(Arrays.asList("a","an","the")); // defining the 3 articles in english
        command = command.toLowerCase().trim();
        command = command.replaceAll("[^a-zA-Z0-9 ]", "");        // replaces characters not in regex
        String[] words = command.split(" ");                       // splitting to check for articles
        String cleanStr = "";

        for (String word : words) {
            if (!articles.contains(word)){
                cleanStr += (word+" ");                       // appending to resultant string if not an article
            }
        }
        cleanStr = cleanStr.trim();                   // trimming the last space
        return cleanStr;
    }

    private String[] getScrollTargets() {
        ScrollTarget[] scrollTargets = ScrollTarget.values();
        String[] targets = new String[scrollTargets.length];

        for (int i = 0; i < scrollTargets.length; i++) {
            targets[i] = scrollTargets[i].name();
        }

        return targets;
    }

    private HashMap<String,String> processTargets(ArrayList<String> uiElements, boolean addAppNames, boolean addUIElements){
        HashMap<String,String> targets = new HashMap<String,String>(); // initialising the array of all available elements

        // adding all the cleaned data to the available targets array
        if(addUIElements){
            for (String element : uiElements) {
                targets.put(cleanElements(element), element);     // passed as arugument as it changes at runtime
            }
        }

        if(addAppNames) {
            for (String app : appNames) {
                targets.put(cleanElements(app), app);     // can be accessed using os functions
            }
        }

        if (!addAppNames && !addUIElements){
            for (String scrollTarget : getScrollTargets()) {
                targets.put(cleanElements(scrollTarget), scrollTarget);
            }
        }

        return targets;
    }

    private String isTriggerWord(String word) {
        for (Action action : actionSynonyms.keySet()) {
            for (String synonym : actionSynonyms.get(action)) {
                if (word.equals(synonym)) {
                    return action.toString();
                }
            }
        }
        return word;
    }

    private boolean isAction(String word) {
        try {
            Action.valueOf(word.toUpperCase());
            return true;
        } catch(IllegalArgumentException exception){
            return false;
        }
    }

    // Use the max length of app name on device, or 25 characters (together)
    private String getMaxTargetString(String command, Integer position){
        int maxIndex = (position+51 < command.length()) ? position+51 : command.length();
        if (position + 1 < command.length()) {
            return command.substring(position+1,maxIndex);
        }
        return "";
    }

    /*
      This method is used to find all the action triggers in a given sentence and
      action -> {"ACTION.CLICK", {"1": [START,END+1], ...} }
    */
    private HashMap<Action,HashMap<Integer,ArrayList<Integer>>> getActionTriggers(String command) {
        HashMap<Action,HashMap<Integer,ArrayList<Integer>>> actions = new HashMap<Action,HashMap<Integer,ArrayList<Integer>>>();
        actionTargetRangeList = new ArrayList<HashMap<Action, String>>();
        String currentWord = "";
        Integer positionIndex = 0, wordIndex = 0;

        if(command.length() > 0){

            // command traversal loop
            while (positionIndex < command.length()){
                currentWord = "";
                wordIndex = positionIndex;    // resetting variables for new word

                // character traversal loop for word extraction
                while(command.charAt(wordIndex) != ' '){
                    currentWord += command.charAt(wordIndex);
                    wordIndex++;
                    if (wordIndex >= command.length()){
                        break;
                    }
                }

                currentWord = isTriggerWord(currentWord);

                if(isAction(currentWord)){
                    Action currentAction = Action.valueOf(currentWord.toUpperCase());
                    HashMap<Action,String> currentActionTargetRange = new HashMap<Action, String>();
                    HashMap<Integer,ArrayList<Integer>> triggers = (actions.containsKey(currentAction)) ? actions.get(currentAction) : new HashMap<Integer,ArrayList<Integer>>();
                    ArrayList<Integer> wordPosition = new ArrayList<Integer>();

                    wordPosition.add(positionIndex);    // start position
                    wordPosition.add(wordIndex);        // end position
                    triggers.put(triggers.size()+1, wordPosition);
                    currentActionTargetRange.put(currentAction,getMaxTargetString(command,wordIndex));
                    actionTargetRangeList.add(currentActionTargetRange);
                    actions.put(currentAction, triggers);
                }

                positionIndex += (wordIndex + 1 - positionIndex);  // moving the command word pointer variable
            }
        }
        return actions;
    }

    /*
    @pre-cond: Needs cleaned command & targets (articles and symbols removed)
    */
    private ArrayList<String> getTargetPossibilities(String command) {
        String[] targets = command.split(" ");
        HashSet<String> targetPossibilities = new HashSet<String>();
        String multiWordPossibility = "";
        String multiWordPossibilityRedundantPrefix = "";
        boolean potentialRedundant = false;

        for (String target : targets) {
            multiWordPossibility+=target;
            if (potentialRedundant) {
                multiWordPossibilityRedundantPrefix += target;
                targetPossibilities.add(multiWordPossibilityRedundantPrefix);
            }
            targetPossibilities.add(multiWordPossibility);
            potentialRedundant = true;
        }
        // System.out.println("\ntarget possibilities: \n");
        // System.out.println(targetPossibilities);
        return new ArrayList<String> (targetPossibilities);
    }

    private boolean isGrammarError(String target, String possibility) {
        // str1 = target (UI Elements / System Packages)
        // str2 = possibility (Command)
        int characterDifferenceThreshold = 3;
        int characterChangeThreshold = characterDifferenceThreshold - (Math.abs(target.length() - possibility.length()));
        int maxComparableLen = Math.min(target.length(), possibility.length());

        // allowing "PlayStore" - "Play Stores"  but not ("GooglePlay" - "Google Plus")
        // A measure to support ()
        boolean contiguousChange = false;

        // allowing only one multiple position change (woman-women)
        boolean multiplePosChange = false;


        if(maxComparableLen < (characterDifferenceThreshold+1)){
            return false;               // base case handling
        }

        for(int i = 0; i<maxComparableLen; i++){
            if(contiguousChange && target.charAt(i) == possibility.charAt(i)){
                contiguousChange = false;
                multiplePosChange = true;
            }

            if(target.charAt(i) != possibility.charAt(i) && !multiplePosChange){
                characterChangeThreshold -= 1;
                contiguousChange = true;
            } else if (target.charAt(i) != possibility.charAt(i) && multiplePosChange) {
                return false;
            }

        }

        if(characterChangeThreshold<0){
            return false;          // exceeds acceptable thresholds
        }
        return true;
    }

    private boolean isAmbiguous(String target, String possibility) {
        String[] targetWords = target.split(" ");
        for (String word : targetWords){
            if(possibility.length() == possibility.replaceFirst(word,"").length()){
                return false;
            }
            possibility = possibility.replaceFirst(word,"");
        }
        if(possibility.length() > 3){
            return false;
        }

        return true;
    }

    private ArrayList<String> getEnterText(String range) {
        String enterText = "";
        String[] words = range.split(" ");

        for (String word : words) {
            if (!isAction(isTriggerWord(word)) && !word.equals("and")) {
                enterText+= (word+" ");
            } else {
                break;
            }
        }

        return new ArrayList<String>(Arrays.asList(enterText.trim()));
    }

    private String getLargestElement(ArrayList<String> elementList){
        int maxElementSizeIndex = 0;
        if (elementList.size() > 0){
            for(int i=0; i<elementList.size();i++){
                if(elementList.get(i).length()>elementList.get(maxElementSizeIndex).length()){
                    maxElementSizeIndex = i;
                }
            }
            return elementList.get(maxElementSizeIndex);
        }
        return "";
    }

    private ArrayList<ArrayList<String>> getAllValidTargets(HashMap<String,String> allTargetPossibilities, String range){
        HashSet<String> allTargets = new HashSet<String>();
        ArrayList<ArrayList<String>> validTargets = new ArrayList<ArrayList<String>>(Arrays.asList(new ArrayList<String>(),new ArrayList<String>(),new ArrayList<String>() ));

        for (String possibility : getTargetPossibilities(range)){
            Log.d("FIT4003_VOICIFY", allTargetPossibilities.toString());
            for (String target : allTargetPossibilities.keySet()) {
                // System.out.println("Possibility: "+possibility+ " Target: "+ target);
                if(target.replace(" ","").equals(possibility)) {
                    if(!allTargets.contains(allTargetPossibilities.get(target))){
                        allTargets.add(allTargetPossibilities.get(target));
                        validTargets.get(0).add(allTargetPossibilities.get(target));
                    }
                } else if(isGrammarError(target, possibility) || isAmbiguous(target,possibility)) {
                    if(!allTargets.contains(allTargetPossibilities.get(target))){
                        allTargets.add(allTargetPossibilities.get(target));
                        validTargets.get(1).add(allTargetPossibilities.get(target));
                    }
                }
            }
        }

        if(validTargets.get(0).size() == 0 && validTargets.get(1).size() == 0) {
            for (String possibility : getTargetPossibilities(range)){
                for (String target : allTargetPossibilities.keySet()) {
                    if(target.contains(possibility) && possibility.length()>= Math.floor(0.5*target.replace(" ", "").length()) && possibility.length() > 3) {
                        if(!allTargets.contains(allTargetPossibilities.get(target))){
                            allTargets.add(allTargetPossibilities.get(target));
                            validTargets.get(2).add(allTargetPossibilities.get(target));
                        }
                    } else if (target.contains(possibility) && possibility.length()>= Math.floor(0.3*target.replace(" ", "").length()) && possibility.length() > 3 && target.replace(" ", "").length() > 15) {
                        if(!allTargets.contains(allTargetPossibilities.get(target))){
                            allTargets.add(allTargetPossibilities.get(target));
                            validTargets.get(2).add(allTargetPossibilities.get(target));
                        }
                    }
                }
            }
        }
        // System.out.println("valid targets by priority array: ");
        // System.out.println(validTargets);
        return validTargets;
    }

    public void sortArrayByLength(ArrayList<String> lst){
        lst.sort((s1, s2) -> s2.length() - s1.length());
    }

    private ArrayList<String> getTargets(Action action, String range, boolean includeAll, String sentence){
        ArrayList<String> targets = new ArrayList<String>();
        HashMap<String,String> allTargetPossibilities = new HashMap<String,String>();
        ArrayList<ArrayList<String>> targetPriorityArrays = new ArrayList<ArrayList<String>>();
        sentence = this.cleanElements(sentence);

        if(action == Action.ENTER) {
            targets = getEnterText(range);
        } else {
            if (action == Action.CLICK) {
                allTargetPossibilities = processTargets(uiElements,true,true);
            } else if (action == Action.OPEN) {
                allTargetPossibilities = processTargets(uiElements,true,false);
            } else {
                allTargetPossibilities = processTargets(uiElements,false,false);
            }

            targetPriorityArrays = getAllValidTargets(allTargetPossibilities,range);

            if(!includeAll){
                if(targetPriorityArrays.get(0).size() > 0){
                    // String largestTarget = getLargestElement(targetPriorityArrays.get(0));
                    // if(!largestTarget.equals("")){
                    //   // risky cond
                    //   // if(targetPriorityArrays.get(1).size() == 0){
                    //   //   targets.add(largestTarget);
                    //   // }
                    //   targets.add(largestTarget);
                    // }
                    String suitableTarget = ".", suitableTargetClean = ".";
                    sortArrayByLength(targetPriorityArrays.get(0));
                    for(String target : targetPriorityArrays.get(0)){
                        Log.d("FIT4003_VOICIFY", sentence + " "+ suitableTarget + " "+ targetPriorityArrays.get(0).toString());
                        if(sentence.indexOf(suitableTargetClean)<sentence.indexOf(cleanElements(target))) {
                            suitableTargetClean = cleanElements(target);
                            suitableTarget = target;
                        }
                    }
                    targets.add(suitableTarget);
                }
                if(targetPriorityArrays.get(1).size() > 0 && targets.size()<2) {
                    sortArrayByLength(targetPriorityArrays.get(1));
                    for(String target: targetPriorityArrays.get(1)){
                        targets.add(target);
                        if(targets.size() >= 2) {break;}
                    }
                }
                if(targets.size()<2){
                    for(String target: targetPriorityArrays.get(2)){
                        targets.add(target);
                        if(targets.size() >= 2) {break;}
                    }
                }
            } else {
                for (ArrayList<String> priorityArray : targetPriorityArrays){
                    for(String target : priorityArray){
                        targets.add(target);
                    }
                }
            }
        }
        return targets;
    }

    public ArrayList<HashMap<Action,ArrayList<String>>> getCommandActionTargets(){
        ArrayList<HashMap<Action,ArrayList<String>>> commandActionTargets = new ArrayList<HashMap<Action,ArrayList<String>>>();

        for (HashMap<Action,String> actionRangePair : actionTargetRangeList) {
            List<Action> actionRangeList = new ArrayList<Action>(actionRangePair.keySet());
            HashMap<Action,ArrayList<String>> commandActionTargetMap = new HashMap<Action,ArrayList<String>>();

            Action currentAction = actionRangeList.get(0);
            ArrayList<String> suggestions = getTargets(currentAction, actionRangePair.get(currentAction),false, this.sentence);

            if(suggestions.size()>0){
                commandActionTargetMap.put(currentAction, suggestions);
                commandActionTargets.add(commandActionTargetMap);
            }
        }
        return commandActionTargets;
    }
}

