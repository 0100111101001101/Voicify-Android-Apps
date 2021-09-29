import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        Main.fetchSynonyms();
    }

    @org.junit.jupiter.api.Test
    void cleanElementsTest() {
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
        assertEquals("",Main.cleanElements(String1));
        assertEquals("",Main.cleanElements(String2));
        assertEquals("",Main.cleanElements(String3));
        assertEquals("",Main.cleanElements(String4));
        assertEquals("hello then and",Main.cleanElements(String5));
        assertEquals("helloan as as",Main.cleanElements(String6));
        assertEquals("this is super long string and next string has dollarsign in middle",Main.cleanElements(String7));
        assertEquals("23",Main.cleanElements(String8));

    }

    @org.junit.jupiter.api.Test
    void processTargetsTest() {
        String[] appNames = new String[]{"PlayStore", "Google","Google Plus","Facebook","Youtube", "Music", "Amazon Store",
                "Alexa Play", "Music downloader", "Smart Launcher", "The New York Times", "Maps", "Duolingo: learn a language for free!"};

        String[] uiElements0 = new String[]{}; // Pass empty array -> returns only appNames
        String[] uiElements1 = new String[]{""};// Pass empty string
        String[] uiElements2 = new String[]{" "};// Pass space string
        String[] uiElements3 = new String[]{null};// Pass null object
        String[] uiElements4 = new String[]{"Facebook", "TWITTER", "YouTube", "Money$App"}; // Duplicate entries, Capitals, symbols in middle

        //The following block add either empty strings or whitespace, these should not be present so the output should be the same as appNames
        assertEquals(appNames.length,Main.processTargets(uiElements0).size());
        // THESE SHOULD NOT HAVE THEIR LENGTHS INCREASED BY 1
        assertEquals(appNames.length+1,Main.processTargets(uiElements1).size());
        assertEquals(appNames.length+1,Main.processTargets(uiElements2).size());

        // Tests if additional targets are correctly added into the output of method and retrievable
        assertTrue(Main.processTargets(uiElements4).containsKey("moneyapp"));
        assertTrue(Main.processTargets(uiElements4).containsValue("Money$App"));
        assertTrue(Main.processTargets(uiElements4).containsKey("twitter"));
        assertFalse(Main.processTargets(uiElements4).containsKey("reddit"));

    }

    @org.junit.jupiter.api.Test
    void isSingularPluralTests() {
        assertTrue(Main.isSingularPlural("maps","map"));
        assertTrue(Main.isSingularPlural("google maps","google map"));
        assertTrue(Main.isSingularPlural("Youtube","Youtubes"));
        assertFalse(Main.isSingularPlural("cats","dogs"));
        assertFalse(Main.isSingularPlural("Google Maps","Google Rewards"));
        assertFalse(Main.isSingularPlural("",""));
        assertFalse(Main.isSingularPlural(""," "));
        //assertTrue(Main.isSingularPlural("",null));
        assertFalse(Main.isSingularPlural("maps"," "));

    }

    @org.junit.jupiter.api.Test
    void isTriggerWordTests() {
        Main.fetchSynonyms();
        assertEquals("maps",(Main.isTriggerWord("maps")));  // Not a valid Trigger word, should return the original string
        assertEquals("",(Main.isTriggerWord("")));      // Empty string, should return empty string
        //System.out.println(isTriggerWord(null));  // null input, should handle error
        // Testing CLICK action synonyms
        assertEquals("CLICK",Main.isTriggerWord("tap"));       //valid action, should return CLICK
        assertEquals("CLICK",Main.isTriggerWord("tAp"));       //valid action, should return CLICK
        assertEquals("CLICK",Main.isTriggerWord("toUch"));     //valid action, should return CLICK
        assertEquals("CLICK",Main.isTriggerWord("press"));     //valid action, should return CLICK
        assertEquals("CLICK",Main.isTriggerWord("click"));     //valid action, should return CLICK
        // Testing SCROLL action synonyms
        assertEquals("SCROLL",Main.isTriggerWord("swipe"));     //valid action, should return SCROLL
        assertEquals("SCROLL",Main.isTriggerWord("move"));      //valid action, should return SCROLL
        assertEquals("SCROLL",Main.isTriggerWord("go"));        //valid action, should return SCROLL
        assertEquals("SCROLL",Main.isTriggerWord("SCROLL"));    //valid action, should return SCROLL
        // Testing OPEN action synonyms
        assertEquals("OPEN",Main.isTriggerWord("launch"));    //valid action, should return OPEN
        assertEquals("OPEN",Main.isTriggerWord("start"));     //valid action, should return OPEN
        assertEquals("OPEN",Main.isTriggerWord("execute"));   //valid action, should return OPEN
        assertEquals("OPEN",Main.isTriggerWord("oPeN"));      //valid action, should return OPEN
        // Testing ENTER action synonyms
        assertEquals("ENTER",Main.isTriggerWord("fill"));      //valid action, should return ENTER
        assertEquals("ENTER",Main.isTriggerWord("search"));    //valid action, should return ENTER
        assertEquals("ENTER",Main.isTriggerWord("type"));      //valid action, should return ENTER
        assertEquals("ENTER",Main.isTriggerWord("ENTER"));     //valid action, should return ENTER
        assertEquals("ENTER!",Main.isTriggerWord("ENTER!"));    // Invalid action should return original string
    }

    @org.junit.jupiter.api.Test
    void isActionTests() {
        // Tests for isAction()
        //Main.fetchSynonyms();
        assertFalse(Main.isAction("tap"));        //valid action, should return FALSE
        assertFalse(Main.isAction("tAp"));        //valid action, should return FALSE
        assertTrue(Main.isAction("click"));      //valid action, should return TRUE
        assertTrue(Main.isAction("CLIcK"));      //valid action, should return TRUE
        assertTrue(Main.isAction("ScRoll"));     //valid action, should return TRUE
        assertTrue(Main.isAction("ScRoll"));         //valid action, should return TRUE
        assertTrue(Main.isAction("OPEn"));       //valid action, should return TRUE
        assertFalse(Main.isAction("OPEn!"));      //valid action, should return FALSE
        assertTrue(Main.isAction("Enter"));      //valid action, should return TRUE
        assertTrue(Main.isAction("Enter"));      //valid action, should return TRUE
    }

    @org.junit.jupiter.api.Test
    void getMaxTargetStringTests() {
        assertEquals("smart launcher we google about the proposal", Main.getMaxTargetString("launch smart launcher we google about the proposal",6)); //valid, should return "smart launcher we google about the proposal"
        assertEquals("youtube music", Main.getMaxTargetString("hello open youtube music",10));  //valid, should return "youtube music"
        assertEquals("down the page and only retrieve up to 51 character", Main.getMaxTargetString("scroll down the page and only retrieve up to 51 characters as that is the maximum allowed length in the app store",6)); //valid, should return "down the page and only retrieve up to 51 character"
        //assertEquals("", Main.getMaxTargetString("",6)); // invalid, should handle errors or fail gracefully
        //System.out.println(getMaxTargetString("launch smart launcher we google about the proposal",-5)); //invalid, should handle errors or fail gracefully
        //System.out.println(getMaxTargetString("click on facebook",20)); //invalid, should handle errors or fail gracefully

    }

    @org.junit.jupiter.api.Test
    void getActionTriggersTests() {
        assertEquals("{OPEN={1=[0, 6]}}", Main.getActionTriggers("launch smart launcher we google about the proposal")); //valid, should return {OPEN={1=[0, 6]}}
        assertEquals("{CLICK={1=[10, 15], 2=[33, 36]}}", Main.getActionTriggers("ok google click youtube and then tap home")); //valid uses of CLICK including synonym handling, should return {CLICK={1=[10, 15], 2=[33, 36]}}
        assertEquals("{SCROLL={1=[12, 18]}, CLICK={1=[28, 31]}}", Main.getActionTriggers("hey google, scroll down and tap chrome")); //valid uses of SCROLL and CLICk, should return {SCROLL={1=[12, 18]}, CLICK={1=[28, 31]}}
        assertEquals("{CLICK={1=[11, 14], 2=[15, 20], 3=[21, 26], 4=[27, 32]}}", Main.getActionTriggers("TESTINGTAP tap Click touch press ebay")); //valid uses of SCROLL and CLICk, should return {CLICK={1=[11, 14], 2=[15, 20], 3=[21, 26], 4=[27, 32]}}
        assertEquals("{SCROLL={1=[6, 11], 2=[12, 16]}}", Main.getActionTriggers("SWIPE swipe move Go")); //valid uses of SCROLL SYNONYMS, should return {SCROLL={1=[6, 11], 2=[12, 16]}}
        assertEquals("{OPEN={1=[0, 6], 2=[15, 20], 3=[21, 25]}}", Main.getActionTriggers("launch eXEcute start OPEN settings"));    //valid uses of OPEN SYNONYMS, {OPEN={1=[0, 6], 2=[15, 20], 3=[21, 25]}}
        assertEquals("{ENTER={1=[0, 5], 2=[6, 10], 3=[11, 17], 4=[18, 22]}}", Main.getActionTriggers("ENTER type search fill username"));       //valid uses of ENTER SYNONYMS,{ENTER={1=[0, 5], 2=[6, 10], 3=[11, 17], 4=[18, 22]}}
        assertEquals("{}", Main.getActionTriggers("hello this message is empty")); //valid, although no Action words used, should return {}
        assertEquals("{}", Main.getActionTriggers("")); //valid, empty string, should return {}

    }

    @org.junit.jupiter.api.Test
    void getActionTargetMapTests() {
        Main.getActionTriggers("ok google click youtube and then home"); //valid uses of CLICK and target (youtube), should return {CLICK=[Youtube]}
        assertEquals("{CLICK=[Youtube]}", Main.getActionTargetMap());

        Main.getActionTriggers("hey google launch facebook"); //valid uses of OPEN synonym (launch) and target (facebook), should return {OPEN=[Facebook]}
        assertEquals("{OPEN=[Facebook]}", Main.getActionTargetMap());

        Main.getActionTriggers("hey google"); //valid, no actions or targets ,should return empty set {}
        assertEquals("{}", Main.getActionTargetMap());

        Main.getActionTriggers("scroll down please"); // valid uses of SCROLL, no target selected as it is not an app. Returns {SCROLL=[]}
        assertEquals("{SCROLL=[]}", Main.getActionTargetMap());

        Main.getActionTriggers("hello search youtube"); //valid uses of ENTER synonym (search) and target (youtube), should return {ENTER=[Youtube]}
        assertEquals("{ENTER=[Youtube]}", Main.getActionTargetMap());

        Main.getActionTriggers(""); //Empty action list, should return empty dataset {}
        assertEquals("{OPEN=[Facebook]}", Main.getActionTargetMap());

    }
}