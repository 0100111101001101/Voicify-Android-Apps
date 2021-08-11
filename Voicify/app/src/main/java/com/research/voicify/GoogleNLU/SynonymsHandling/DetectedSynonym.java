package com.research.voicify.GoogleNLU.SynonymsHandling;

import java.util.List;


/**
 *This is a class represent a single synonym returned from the synonym API call
 *
 * @author  Dustin
 * @version 1.0
 * @since   2021-08-11
 */
public class DetectedSynonym {
    private String word;
    private int score;
    private List<String> tag;

    /**
     * This is the constructor of a synonym.
     * @param word This is the string represent the synonym\.
     * @param score  This is the level of relevance
     * @param tag This is a list of tags ( v,n,adj...)
     * @return int This returns sum of numA and numB.
     */
    public DetectedSynonym(String word, int score, List<String> tag) {
        this.word = word;
        this.score = score;
        this.tag = tag;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getTag() {
        return tag;
    }

    public void setTag(List<String> tag) {
        this.tag = tag;
    }
}
