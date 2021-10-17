import java.util.*;

/*
1) Actions:
  i) Extraction
  ii) Synonym handling

2) Targets (UI Elements):
  i) Extraction
    a) Multi Word
    b) Articles Handling (only Articles allowed subsequent to the trigger word)

  ii) Verification
    a) Singular/Plural handling (Dynamic Thresholds)
    b) Articles handling (remove articles from both command app name & device app name) 
    c) Symbols handling
    
  iii) Suggestions 
    a) Faulty/Ambiguous App Names
    b) Wrong/unnecessary words in app names
    c) App not found
    d) App names in other app names

3) Commands:
  i) Command chaining / Conjunctions
    a) Split by: ["and", "or", "not", "then", "after that"]
    b) assign sequence number / id 
    c) assign boolean tags

Maintain a collection of commands as each command is attached a sequence number / id, we can use it for talkback related 

Classes & Data structures planned for use:
  * HashSet for Commands (Unique action & targets)
  * Command Class having action, target, boolean tag & the ID.
  * Action Class having synonyms and action type
  * HashMap for actions (key: enum action, value: Action Obj)
  * HashMap for applications (key: App Name, value: APK Packages
*/

class Main {
    /**
     * String Interpretation Algorithm
     * Developed & written by: Om
     * Method Commenting & testing: Alex
     */
    enum Action {
        CLICK,
        SCROLL,
        OPEN,
        ENTER
    }

    public static String[] appNames = new String[]{"PlayStore", "Google","Google Plus","Facebook","Youtube", "Music", "Amazon Store", "Alexa Play", "Music downloader", "Smart Launcher", "The New York Times", "Maps", "Duolingo: learn a language for free!"};   // to emulate android api response

    public static String[] uiElements = new String[]{"Search Here ...", "directions", "home", "work", "restaurants", "petrol", "groceries", "go", "saved", "contribute", "updates", "FACEBOOK"};   // to emulate runtime ui elements


    public static HashMap<Action,ArrayList<String>> actionSynonyms = new HashMap<Action,ArrayList<String>>();

    public static HashMap<String,String> allTargets = new HashMap<String,String>();

    public static ArrayList<HashMap<Action, String>> actionTargetRangeList = new ArrayList<HashMap<Action, String>> ();

    //Pair<String, String> keyValue = new ImmutablePair("key", "value");


    /**
    This method is used to mock an API Call from Synonyms REST API.

    @post-cond: adds all the synonyms for actions enum in the global variable
                (Key: Action enum, Value: Synonyms of action)
    */
    public static void fetchSynonyms(){
        ArrayList<String> clickSynonyms = new ArrayList<String>(Arrays.asList("tap","touch","press"));
        ArrayList<String> scrollSynonyms = new ArrayList<String>(Arrays.asList("swipe","move","go"));
        ArrayList<String> openSynonyms = new ArrayList<String>(Arrays.asList("launch","start","execute"));
        ArrayList<String> enterSynonyms = new ArrayList<String>(Arrays.asList("fill","search","type"));
        actionSynonyms.put(Action.CLICK, clickSynonyms);
        actionSynonyms.put(Action.SCROLL, scrollSynonyms);
        actionSynonyms.put(Action.OPEN, openSynonyms);
        actionSynonyms.put(Action.ENTER, enterSynonyms);
    }

    /**
     * Alex's interpretation: This method takes a String array of elements, which are then cleaned using
     * cleanElements(). First it iterates through all elements in input (which are assumed to be UI elements on a
     * specific screen) and populates a hashmap using the 'cleaned' value as the key along with the original value.
     *
     * Additionally, an array of application packages retrieved from the Android OS are processed through as well.
     * If an existing key is passed then the previous value gets replaced by the new value.
     *
     @param: String Array containing UI elements on screen
     @Returns: Hashmap with potential targets where key is the cleaned string and value is the original string
     */
    public static HashMap<String,String> processTargets(String[] uiElements){
        HashMap<String,String> targets = new HashMap<String,String>(); // initialising the array of all available elements

        // adding all the cleaned data to the available targets array

        for (String element : uiElements) {
            targets.put(cleanElements(element), element);     // passed as arugument as it changes at runtime
        }

        for (String app : appNames) {
            targets.put(cleanElements(app), app);     // can be accessed using os functions
        }

        return targets;
    }

    /**
     * Alex's interpretation: This method creates a HashMap which maps the ACTION keyword to an arraylist containing
     * the target application that is to be addressed
     *
     @Returns: Hashmap where KEY is an ACTION keyword, and the VALUE is an ArrayList containing the 'target' string,
     * which is the application name to have the action performed on
     */
    public static HashMap<Action,ArrayList<String>> getActionTargetMap() {
        HashMap<Action,ArrayList<String>> matches = new HashMap<Action,ArrayList<String>>();
        // Loop through each actionTargetRangeList element. key: Action, value: string
        for (HashMap<Action,String> actionRangePair : actionTargetRangeList) {
            // Create a list of the keys from each actionrangepair, this is a list of Actions
            List<Action> actionRangeList = new ArrayList<Action>(actionRangePair.keySet());
            // get the first action
            Action currentAction = actionRangeList.get(0);
            // get string value for current action
            String currentRange = actionRangePair.get(currentAction);
            HashSet<String> targets = new HashSet<String>();
            // iterate through each string possibility in the current action range
            for (String possibility : getTargetPossibilities(currentRange)){
                for (String target : allTargets.keySet()) {
                    // System.out.println("Possibility: "+possibility+ " Target: "+ target);
                    // clean target string by removing whitespace and check if target possibility matches to any targets on device
                    if(target.replace(" ","").equals(possibility) || isSingularPlural(target.replace(" ",""), possibility)) {
                        targets.add(allTargets.get(target));
                    }
                }
            }

            matches.put(currentAction,new ArrayList<String>(targets));
            //System.out.println("\n"+currentAction);
            //System.out.println(matches.get(currentAction));
        }

        return matches;
    }

    // private static ArrayList<String> getSuggestions() {
    //   ArrayList<String> suggestions = new ArrayList<String> ();

    //   return suggestions;
    // }

    /**
     * Alex's interpretation: This method takes two string inputs, a target and possibility respectively and performs
     * string comparison, and applies english language logic to determine if two strings are singular/plural of eachother.
     * @param str1
     * @param str2
     * @return True if inputs are matching singular/plural of eachother, otherwise returns false
     * TODO: Does not seem to work 100% correct, you can pass two strings of similar length and it will still reutrn true e.g. "map" and "masp"
     */
    public static boolean isSingularPlural(String str1, String str2) {
        int characterChangeThreshold = 2 - (Math.abs(str1.length() - str2.length()));
        int maxComparableLen = str1.length() < str2.length() ? str1.length() : str2.length();
        boolean contiguousChange = false;
        boolean multiplePosChange = false;

        if(maxComparableLen < 3){
            return false;
        }

        for(int i = 0; i<maxComparableLen;i++){
            if(contiguousChange && str1.charAt(i) == str2.charAt(i)){
                contiguousChange = false;
                multiplePosChange = true;
            }

            if(str1.charAt(i) != str2.charAt(i) && !multiplePosChange){
                characterChangeThreshold -= 1;
                contiguousChange = true;
            } else if (str1.charAt(i) != str2.charAt(i) && multiplePosChange) {
                return false;
            }

        }

        if(characterChangeThreshold<0){
            return false;          // exceeds acceptable thresholds
        }
        return true;
    }


    /***
     * Alex's Interpretation: This method is used to find all the action triggers in a given sentence and creates a HashMap with their occurences and indexing
     * This also takes care of handling synonyms and matching them back to their registered ACTION enum
     * Key: an ACTION keyword
     * Value: HashMap containing occurrence integer as key and Value of ArrayLst containing 2 integers to specify the start and end index of Keyword in command string
     *
     * @param command string to be looked up to determine if it is a 'trigger' word is used which may be a synonym
     * @return HashMap - Containing ACTION keyword as KEY, with Value being a HashMap indexed by an occurence number which is matched up to an
     * arraylist value, which contains the start and ending index of action word inside command string e.g. [34,40]
     * Example output: {OPEN={1=[49, 55]}}
    */
    public static HashMap<Action,HashMap<Integer,ArrayList<Integer>>> getActionTriggers(String command) {
        HashMap<Action,HashMap<Integer,ArrayList<Integer>>> actions = new HashMap<Action,HashMap<Integer,ArrayList<Integer>>>();
        actionTargetRangeList = new ArrayList<HashMap<Action, String>>();
        String currentWord = "";
        Integer positionIndex = 0, wordIndex = 0;

        if(command.length() > 0){

            // command loop
            while (positionIndex < command.length()){
                currentWord = "";
                wordIndex = positionIndex;    // resetting variables for new word

                // character loop
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

    /**
     * Alex's interpretation: This method takes a string as input and then looks it up against the action synonyms available
     * to match the input if it is a synonym, to corresponding ACTION keyword, otherwise returns original string
     *
     * @param word string to be looked up to determine if it is a 'trigger' word is used which may be a synonym
     * @return string - action keyword that corresponds to the string (which may be a synonym) if it exists, otherwise returns the original input
     */
    public static String isTriggerWord(String word) {
        String wordLookup = word.toUpperCase();
        for (Action action : actionSynonyms.keySet()) {
            for (String synonym : actionSynonyms.get(action)) {
                if (wordLookup.equals(synonym.toUpperCase()) || wordLookup.equals(action.toString())) {
                    return action.toString();
                }
            }
        }
        return word;
    }

    /**
     * Alex's Interpretation: This method takes a string input representing an action, which is then looked up to the ACTION
     * enum data structure, before returning a boolean if the string is a registered ACTION. This is generally called
     * after first running through isTriggerWord() to ensure synonym words are matched to their base action.
     *
     * @param word string to be looked up to determine if it is a 'action' word
     * @return Boolean
     */
    public static boolean isAction(String word) {
        try {
            Action.valueOf(word.toUpperCase());
            return true;
        } catch(IllegalArgumentException exception){
            return false;
        }
    }

    /**
     * Alex's Interpretation: This method retrieves the potential target string as a substring, from the inputted command.
     * It takes an integer parameter which specifies the starting index for the substring retrieval to start from.
     *
     * @param command string inputted by user
     * @param position integer indicating the starting position of the substring to be retrieved, typically this is the
     *                 index of the last character of the ACTION keyword that prepends the string
     * @return String containing up to 51 character substring starting from the position specified in the input parameter
     */
    public static String getMaxTargetString(String command, Integer position){
        int maxIndex = (position+51 < command.length()) ? position+51 : command.length();
        return command.substring(position+1,maxIndex);
    }

    /**
     * Alex's Interpretation: This method takes a command string as input and extracts all possible targets into an arraylist
     * which contains singular words and in somecases, multi-word strings for apps that have more than 1 word in its name
     *
     * @param command string inputted by user
     * @pre-cond: Needs cleaned command & targets (articles and symbols removed)
     * @return ArrayList containing all possible app targets onscreen
    */
    private static ArrayList<String> getTargetPossibilities(String command) {
        String[] targets = command.split(" ");
        HashSet<String> targetPossibilities = new HashSet<String>();
        String multiWordPossibility_nr = "";
        String multiWordPossibility_r = "";
        boolean potentialRedundant = false;

        // iterate through targets array, and populate targetpossibilities hashset, firstly with a single 'target'
        for (String target : targets) {
            multiWordPossibility_nr+=target;
            // Will always not run on first iteration
            if (potentialRedundant) {
                // After the Second target element, this string gets linearly larger NOT SURE IF INTENTIONAL
                multiWordPossibility_r += target;
                // adds multiWordPossibility_r to target possibilities, which can be any target after the first
                // if this element is redundant, it will not create a duplicate entry in the targetPossibilities hashset
                // if it is not redundant, the target will be added
                targetPossibilities.add(multiWordPossibility_r);
            }

            targetPossibilities.add(multiWordPossibility_nr);
            potentialRedundant = true;
        }
        //System.out.println(targetPossibilities);
        return new ArrayList<String> (targetPossibilities);
    }

    /**
    This method is used to clean the given string. It removes all the characters that are not possible to be produced by
    speech to text recognition listener using regex. It removes all the articles from the string.

    @param: This is the string that will be used to handle symbols and articles.
    @return: It returns a string that is cleaned.
    */
    // TODO: CONSIDER ADDING NULL HANDLING TO IMPROVE ROBUSTNESS
    public static String cleanElements(String command){
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

    public static void main(String[] args) {
        // catches: google and play store are two different words & apps, open & start are both trigger words, "the" after a trigger statement, use of plural instead of singular, multiple trigger words.
        // expected: Suggest Google or Play Store
        String sentence48 = "Hey I can start you so can you please open the google play stores and after that maybe I can start working on the proposal";
        //String sentence1 = "open the google play stores";


        // catches: start is a trigger statement and youtube is in the threshold but it shall not be opened, downloader has a trigger word load and google which is an app name is within the threshold, NLP may not be able to recognise "Music downloader" as an app but would rather return "Music" as a app, "the" after a trigger statement, Multiple app possibility, plural usage.
        // expected: open Music downloader
        String sentence49 = "I can start you but ignore youtube and google some stuff after that launch the Google Map then tap on search here and search kfc and swipe down";

        // catches: launch is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word.
        // expected: launch smart launcher
        String sentence50 = "Hey I like android development so can you please launch smart launcher we google about the proposal";

        // catches: launch is a trigger word
        // expected: launch google
        String sentence1 = "Launch google";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word.
        // expected: ask to open google, or playstore
        String sentence2 = "Open Google Play store";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. There is an article after trigger word
        // expected: open google maps
        String sentence3 = "Open the Google Maps";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. The name of the application is wrong but more than 30% match of app string should provide the relevant options
        // expected: open musics, musics downloader
        String sentence4 = "Open Musicals Player";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. Contains two app names after trigger word.
        // expected: open messenger, facebook
        String sentence5 = "Open Facebook Messenger";

        // catches: swipe is a trigger word, a direction is given after the trigger word to scroll right
        // expected: should scroll right
        String sentence6 = "Swipe Right";

        // catches: launch is a trigger word, an app name is in the 50 char threshold range of the trigger word. It is handling a multiword app
        // expected: open uber eats
        String sentence7 = "Launch Uber Eats";

        // catches: open is a trigger word. There are two application names after the trigger word. Both applications are joint by a conjuction.
        // expected: open uber eats or deliveroo
        String sentence8 = "Open UberEats or Deliveroo";

        // catches: contains two trigger words and an app name
        // expected: start uber eats
        String sentence9 = "open start Uber Eats";

        // catches: open is the trigger word, contains 3 trigger words and 3 application
        // expected: --
        String sentence10 = "Open Musical Player open musics player open store open musics";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. There are two article after trigger word
        // expected: open doordash
        String sentence11 = "Open the the Doordash";

        // catches: launch is a trigger word, an app name is in the 50 char threshold range of the trigger word. App name is wrongly spelt. It is pluralised
        // expected: launch uber eats
        String sentence12 = "Launch uber eat umm yes haha";

        // catches: launch is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. Contains a lot of unnecessary details about the application
        // expected: launch asana
        String sentence13 = "Launch the asana project management app";

        // catches: contains two trigger words and single application name
        // expected: start google maps
        String sentence14 = "Click Start Google Maps";

        // catches: launch is a trigger word and the contains two app names which are not joined by a conjunction
        // expected: -
        String sentence15 = "Launch Uber eats doordash";

        // catches: open is a trigger word, consist of two app names and a subject all joined by conjunctions 
        // expected: -
        String sentence16 = "Open UberEats or Deliveroo and order a big mac but not from McDonald's Clayton";

        // catches: start is a trigger word, an app name is in the 50 char threshold range of the trigger word. There are two article after trigger word. The app name is wrong, so there should be over 30% match 
        // expected: did you mean doordash?
        String sentence17 = "Start a doortrash";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. The multiple word app name is joined to be one
        // expected: open uber eats
        String sentence18 = "open ubereats";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. The second command is enter 24 mchicken nuggets
        // expected: open doordash and enter 24 mcchicken nuggets. Doordash spelling has been altered.
        String sentence19 = "open door dash and enter 24 mcchicken nuggets";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. Asks to open the following applications.
        // expected: open facebook, messenger, google, playstore
        String sentence20 = "Open the Facebook messenger and open Google play store";

        // catches: open is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. There is a article and app name is google playstore
        // expected: open google, playstore
        String sentence21 = "open a google play store";

        // catches: launch is a trigger word, an app name is in the 50 char threshold range of the trigger word. The app name is ambiguous thus asks to open application which has more than 30% match
        // expected: launch playstore, samsung store, ...
        String sentence22 = "Launch store";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. The Uber eat spelling is wrong 
        // expected: open uber eats and enter 24 nuggets
        String sentence23 = "open Uber Eat and enter 24 nuggets";

        // catches: open is a trigger word, an app name is in the 50 char threshold range of the trigger word. There is an article after trigger word
        // expected: open maps, google maps
        String sentence24 = "start a Maps";

        // catches: start is a trigger word, an app name is in the 50 char threshold range of the trigger word. There is another trigger word search and subject home
        // expected: start google maps and search home
        String sentence25 = "start google maps and search home";

        // catches: launch is a trigger word, an app name is in the 50 char threshold range of the trigger word. The spelling of the application is wrong. There si another trigger word open which is followed by amazon application.
        // expected: -
        String sentence26 = "launch google map and open amazon application";

        // catches: launch is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. Two trigger words and 4 possible application thatg needs to be opened
        // expected: launch Facebook, Messenger and open Maps, google maps
        String sentence27 = "launch Facebook Messenger and open Maps";

        // catches: start is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. It has a article after the trigger word
        // expected: start google maps
        String sentence28 = "start a Google maps";
        
        // catches: open is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. Has conjunction.
        // expected: open doordash, uber eats
        String sentence29 = "Open doordash or Uber Eats";

        // catches: start is the trigger word, engine is not an application. There is another trigger word open which is joined to the first command through conjunction and is followed by messeger.
        // expected: open messenger
        String sentence30 = "start the engine and open messenger";

        // catches: open is the trigger word which is followed by messenger which is an application. It has also a lot of words which are not required.
        // expected: open messenger
        String sentence31 = "Hey, this is great, ill open messenger to text you";

        // catches: open, scroll and enter are the trigger words. They are followed by an application or an action. It has 2 conjunction.
        // expected: open google maps, scroll down, enter home
        String sentence32 = "Open Google Maps and then scroll down and enter home";

        // catches: open is the trigger word which is followed by ubereats and deliveroo which is an application. It has alot of conjunction such as not and 'and' and or.
        // expected: open messenger
        String sentence33 = "Open UberEats or Deliveroo and order a big mac but not from McDonald's Clayton";

        // catches: open is the trigger word which is followed by google which is an application. it also has another trigger word fill which is followed by home 
        // expected: open google, fill home
        String sentence34 = "open Google and fill home";

        // catches: swipe and open are th trigger words, they are followed by an action  and a ambiguous app name.
        // expected: swipe right, open google maps, maps
        String sentence35 = "Swipe right and breaking open maps";

        // catches: open is the trigger word which is followed by moogle traps which is an application. It is spelled absolutely wrong.
        // expected: open google maps
        String sentence36 = "open Moogle Traps";

        // catches: launch is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. The spelling of the application is wrong. 
        // expected: launch google maps, swipe right
        String sentence37 = "launch moogle traps or swipe right";

        // catches: no trigger word, enter big mac inside uber eats application
        // expected: -
        String sentence38 = "enter big mac from Uber Eats";

        // catches: start, launch are the trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. Two trigger words and 4 possible application thatg needs to be opened
        // expected: start Facebook, Messenger and open Maps, google maps
        String sentence39 = "Start launch a Facebook Messenger or a Maps";

        // catches: open is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. Uses a conjunction or.
        // expected: open doordash, uber eats
        String sentence40 = "Open Moordash or Suber beats";

        // catches: open, launch are the trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word.
        // expected: launch google maps
        String sentence41 = "open launch google maps";

        // catches: open, launch are the trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. It si also followed by an article
        // expected: launch google maps
        String sentence42 = "Open launch a google maps";

        // catches: start is a trigger word and the app name contains a trigger word, an app name is in the 50 char threshold range of the trigger word. The spelling of the application is wrong. Also involves an article.
        // expected: start asana
        String sentence43 = "Start an Ahana";

        // catches: start and open are the trigger word, app name is in the 50 char threshold range of the trigger word. The spelling of doordash and uber eats is wrong. There is an article after open. The sentences are joined by an conjunction.
        // expected: start doordash, open uber eat
        String sentence44 = "Hey maybe start Doormash and open the Uber Eat";

        // catches: No trigger word. Just faulty app name.
        // expected:-
        String sentence45 = "Musics Player";

        // catches: No trigger word. two application name and a conjunction
        // expected:-
        String sentence46 = "Uber eats not from doordash";

        // catches: No trigger word. Just app name with an article.
        // expected:-
        String sentence47 = "a uber eats";

        // catches: No trigger word. Just app name without any spaces.
        // expected:-
        String sentence51 = "Ubereats";

    /*
    Cleaning a string refers to the following,
      i) Removing whitespaces
      ii) All lower case
      iii) Symbols Removal
      iv) Articles Removal
    */

        // STEP 1: Check synonyms, if absent, fetching synonyms from REST API & caching it
        fetchSynonyms();

        // STEP 2: Cleaning all the possible UI Targets () and Commands for the algorithm
        allTargets = processTargets(uiElements); // TODO: HashMap for recovery to original text
        sentence1 = cleanElements(sentence1);
        sentence2 = cleanElements(sentence2);   // cleaning the command
        sentence3 = cleanElements(sentence3);
        sentence4 = cleanElements(sentence4);
        sentence5 = cleanElements(sentence5);
        sentence6 = cleanElements(sentence6);
        sentence7 = cleanElements(sentence7);
        sentence8 = cleanElements(sentence8);
        sentence9 = cleanElements(sentence9);
        sentence10 = cleanElements(sentence10);
        sentence11 = cleanElements(sentence11);
        sentence12 = cleanElements(sentence12);
        sentence13 = cleanElements(sentence13);
        sentence14 = cleanElements(sentence14);
        sentence15 = cleanElements(sentence15);
        sentence16 = cleanElements(sentence16);
        sentence17 = cleanElements(sentence17);
        sentence18 = cleanElements(sentence18);
        sentence19 = cleanElements(sentence19);
        sentence20 = cleanElements(sentence20);
        sentence21 = cleanElements(sentence21);
        sentence22 = cleanElements(sentence22);
        sentence23 = cleanElements(sentence23);
        sentence24 = cleanElements(sentence24);
        sentence25 = cleanElements(sentence25);
        sentence26 = cleanElements(sentence26);
        sentence27 = cleanElements(sentence27);
        sentence28 = cleanElements(sentence28);
        sentence29 = cleanElements(sentence29);
        sentence30 = cleanElements(sentence30);
        sentence31 = cleanElements(sentence31);
        sentence32 = cleanElements(sentence32);
        sentence33 = cleanElements(sentence33);
        sentence34 = cleanElements(sentence34);
        sentence35 = cleanElements(sentence35);
        sentence36 = cleanElements(sentence36);
        sentence37 = cleanElements(sentence37);
        sentence38 = cleanElements(sentence38);
        sentence39 = cleanElements(sentence39);
        sentence40 = cleanElements(sentence40);
        sentence41 = cleanElements(sentence41);
        sentence42 = cleanElements(sentence42);
        sentence43 = cleanElements(sentence43);
        sentence44 = cleanElements(sentence44);
        sentence45 = cleanElements(sentence45);
        sentence46 = cleanElements(sentence46);
        sentence47 = cleanElements(sentence47);
        sentence48 = cleanElements(sentence48);
        sentence49 = cleanElements(sentence49);
        sentence50 = cleanElements(sentence50);
        sentence51 = cleanElements(sentence51);

        // clean synonyms?

        // STEP 3: Extracting information / assigning meaning

        System.out.println("\n\nAll system targets (Apps + ui elements):");
        System.out.println(allTargets);

        System.out.println("\n\nSentence 1 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence1));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 2 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence2));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 4 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence4));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 5 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence5));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 6 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence6));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 7 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence7));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 8 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence8));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 9 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence9));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 10 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence10));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 11 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence11));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 12 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence12));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 13 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence13));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 14 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence14));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 15 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence15));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 16 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence16));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 17 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence17));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 18 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence18));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 19 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence19));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 20 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence20));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 21 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence21));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 22 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence22));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 23 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence23));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 24 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence24));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 25 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence25));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 26 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence26));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 27 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence27));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 28 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence28));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 29 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence29));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 30 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence30));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 31 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence31));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 32 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence32));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 33 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence33));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 34 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence34));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 35 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence35));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 36 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence36));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 37 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence37));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 38 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence38));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 39 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence39));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 40 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence40));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 41 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence41));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 42 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence42));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 43 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence43));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 44 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence44));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 45 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence45));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 46 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence46));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 47 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence47));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 48 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence48));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 49 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence49));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 50 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence50));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();

        System.out.println("\n\nSentence 51 Extraction:");
        System.out.println("\nActions Extraction:");
        System.out.println(getActionTriggers(sentence51));
        System.out.println("\nActions & Target Range:");
        System.out.println(actionTargetRangeList);
        System.out.println("\nMatches:");
        getActionTargetMap();


        System.out.println(getActionTargetMap());
        // System.out.println("\n\n");
        // System.out.println(getTargetPossibilities("smart launcher we google proposal"));
        // System.out.println("\n\n");

        // cleanElements("Duolingo: learn a language for free!"); // app name
        // cleanElements("open the duolingo app"); // command


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Tests for cleanElements()
        String String0 = null; // Breaks cleanElements, needs null handling
        String String1 = ""; // Empty string
        String String2 = " ";// Space string
        String String3 = "   ";// Tab string
        String String4 = "AN a$ the a"; //
        String String5 = "HELLO AN a tHe then and $$$";
        String String6 = "HELLOan AN aS a$s the";
        String String7 = "this IS a Super long string and the next string has a dollarsign in the middle: a$n";
        String String8 = "%^&%#$@23 t%he a the ";

        //System.out.println(cleanElements(String0));
        System.out.println(cleanElements(String1));
        System.out.println(cleanElements(String2));
        System.out.println(cleanElements(String3));
        System.out.println(cleanElements(String4));
        System.out.println(cleanElements(String5));
        System.out.println(cleanElements(String6));
        System.out.println(cleanElements(String7));
        System.out.println(cleanElements(String8));


        // Tests for processTargets()

        // appNames from above, used for reference
        //appNames = new String[]{"PlayStore", "Google","Google Plus","Facebook","Youtube", "Music", "Amazon Store",
        //        "Alexa Play", "Music downloader", "Smart Launcher", "The New York Times", "Maps", "Duolingo: learn a language for free!"};
        String[] uiElements0 = new String[]{}; // Pass empty array -> returns only appNames
        String[] uiElements1 = new String[]{""};// Pass empty string
        String[] uiElements2 = new String[]{" "};// Pass space string
        String[] uiElements3 = new String[]{null};// Pass null object
        String[] uiElements4 = new String[]{"Facebook", "TWITTER", "YouTube", "Money$App"}; // Duplicate entries, Capitals, symbols in middle

        System.out.println(processTargets(uiElements0));
        System.out.println(processTargets(uiElements1));
        System.out.println(processTargets(uiElements2));
        //System.out.println(processTargets(uiElements3)); // Pass null array *breaks*
        System.out.println(processTargets(uiElements4));


        // Tests for isSingularPlural()

        System.out.println(isSingularPlural("maps","map"));
        System.out.println(isSingularPlural("google maps","google map"));
        System.out.println(isSingularPlural("Youtube","Youtubes"));
        System.out.println(isSingularPlural("cats","dogs"));
        System.out.println(isSingularPlural("Google Maps","Google Rewards"));
        System.out.println(isSingularPlural("",""));
        System.out.println(isSingularPlural(""," "));
        //System.out.println(isSingularPlural("",null));
        System.out.println(isSingularPlural("maps"," "));


        // Tests for isTriggerWord()
        System.out.println(isTriggerWord("maps"));  // Not a valid Trigger word, should return the original string
        System.out.println(isTriggerWord(""));      // Empty string, should return empty string
        //System.out.println(isTriggerWord(null));  // null input, should handle error
        // Testing CLICK action synonyms
        System.out.println(isTriggerWord("tap"));       //valid action, should return CLICK
        System.out.println(isTriggerWord("tAp"));       //valid action, should return CLICK
        System.out.println(isTriggerWord("toUch"));     //valid action, should return CLICK
        System.out.println(isTriggerWord("press"));     //valid action, should return CLICK
        System.out.println(isTriggerWord("click"));     //valid action, should return CLICK
        // Testing SCROLL action synonyms
        System.out.println(isTriggerWord("swipe"));     //valid action, should return SCROLL
        System.out.println(isTriggerWord("move"));      //valid action, should return SCROLL
        System.out.println(isTriggerWord("go"));        //valid action, should return SCROLL
        System.out.println(isTriggerWord("SCROLL"));    //valid action, should return SCROLL
        // Testing OPEN action synonyms
        System.out.println(isTriggerWord("launch"));    //valid action, should return OPEN
        System.out.println(isTriggerWord("start"));     //valid action, should return OPEN
        System.out.println(isTriggerWord("execute"));   //valid action, should return OPEN
        System.out.println(isTriggerWord("oPeN"));      //valid action, should return OPEN
        // Testing ENTER action synonyms
        System.out.println(isTriggerWord("fill"));      //valid action, should return ENTER
        System.out.println(isTriggerWord("search"));    //valid action, should return ENTER
        System.out.println(isTriggerWord("type"));      //valid action, should return ENTER
        System.out.println(isTriggerWord("ENTER"));     //valid action, should return ENTER
        System.out.println(isTriggerWord("ENTER!"));    // Invalid action should return original string

        // Tests for isAction()
        System.out.println(isAction("tap"));        //valid action, should return FALSE
        System.out.println(isAction("tAp"));        //valid action, should return FALSE
        System.out.println(isAction("click"));      //valid action, should return TRUE
        System.out.println(isAction("CLIcK"));      //valid action, should return TRUE
        System.out.println(isAction("ScRoll"));     //valid action, should return TRUE
        System.out.println(isAction("ScRoll"));         //valid action, should return TRUE
        System.out.println(isAction("OPEn"));       //valid action, should return TRUE
        System.out.println(isAction("OPEn!"));      //valid action, should return FALSE
        System.out.println(isAction("Enter"));      //valid action, should return TRUE
        System.out.println(isAction("Enter"));      //valid action, should return TRUE

        // Tests for getMaxTargetString(String command, Integer position)
        System.out.println(getMaxTargetString("launch smart launcher we google about the proposal",6)); //valid, should return "smart launcher we google about the proposal"
        System.out.println(getMaxTargetString("hello open youtube music",10));  //valid, should return "youtube music"
        System.out.println(getMaxTargetString("scroll down the page and only retrieve up to 51 characters as that is the maximum allowed length in the app store",6)); //valid, should return "down the page and only retrieve up to 51 character"
        //System.out.println(getMaxTargetString("",6)); // invalid, should handle errors or fail gracefully
        //System.out.println(getMaxTargetString("launch smart launcher we google about the proposal",-5)); //invalid, should handle errors or fail gracefully
        //System.out.println(getMaxTargetString("click on facebook",20)); //invalid, should handle errors or fail gracefully

        // Tests for getActionTriggers(String command)
        System.out.println(getActionTriggers("launch smart launcher we google about the proposal")); //valid, should return {OPEN={1=[0, 6]}}
        System.out.println(getActionTriggers("ok google click youtube and then tap home")); //valid uses of CLICK including synonym handling, should return {CLICK={1=[10, 15], 2=[33, 36]}}
        System.out.println(getActionTriggers("hey google, scroll down and tap chrome")); //valid uses of SCROLL and CLICk, should return {SCROLL={1=[12, 18]}, CLICK={1=[28, 31]}}
        System.out.println(getActionTriggers("TESTINGTAP tap Click touch press ebay")); //valid uses of SCROLL and CLICk, should return {CLICK={1=[11, 14], 2=[15, 20], 3=[21, 26], 4=[27, 32]}}
        System.out.println(getActionTriggers("SWIPE swipe move Go home")); //valid uses of SCROLL SYNONYMS, should return {SCROLL={1=[0, 5], 2=[6, 11], 3=[12, 16], 4=[17, 19]}}
        System.out.println(getActionTriggers("launch eXEcute start OPEN settings"));    //valid uses of OPEN SYNONYMS, {OPEN={1=[0, 6], 2=[15, 20], 3=[21, 25]}}
        System.out.println(getActionTriggers("ENTER type search fill username"));       //valid uses of ENTER SYNONYMS,{ENTER={1=[0, 5], 2=[6, 10], 3=[11, 17], 4=[18, 22]}}
        System.out.println(getActionTriggers("hello this message is empty")); //valid, although no Action words used, should return {}
        System.out.println(getActionTriggers("")); //valid, empty string, should return {}

        // Tests for getActionTargetMap()

        System.out.println(getActionTriggers("ok google click youtube and then home")); //valid uses of CLICK and target (youtube), should return {CLICK=[Youtube]}
        System.out.println("Matches: " + getActionTargetMap());

        System.out.println(getActionTriggers("hey google launch facebook")); //valid uses of OPEN synonym (launch) and target (facebook), should return {OPEN=[Facebook]}
        System.out.println("Matches: " + getActionTargetMap());

        System.out.println(getActionTriggers("hey google")); //valid, no actions or targets ,should return empty set {}
        System.out.println("Matches: " + getActionTargetMap());

        System.out.println(getActionTriggers("scroll down please")); // valid uses of SCROLL, no target selected as it is not an app. Returns {SCROLL=[]}
        System.out.println("Matches: " + getActionTargetMap());

        System.out.println(getActionTriggers("hello search youtube")); //valid uses of ENTER synonym (search) and target (youtube), should return {ENTER=[Youtube]}
        System.out.println("Matches: " + getActionTargetMap());

        System.out.println(getActionTriggers("")); //Empty action list, should return empty dataset {}
        System.out.println("Matches: " + getActionTargetMap());
    }

}

