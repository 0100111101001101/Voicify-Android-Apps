package com.research.voicify.GoogleNLU;


import java.util.ArrayList;
import java.util.List;

public class RespondBody {
    public RespondBody(Payload[] payload) {
        this.payload = payload;
    }

    public Payload[] getPayload() {
        return payload;
    }

    public void setPayload(Payload[] payload) {
        this.payload = payload;
    }

    public Payload[] payload;


    public static class Payload {
        public TextExtraction textExtraction;

        public TextExtraction getTextExtraction() {
            return textExtraction;
        }

        public void setTextExtraction(TextExtraction textExtraction) {
            this.textExtraction = textExtraction;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public Payload(TextExtraction textExtraction, String displayName) {
            this.textExtraction = textExtraction;
            this.displayName = displayName;
        }

        public static class TextExtraction {
            public TextExtraction(TextSegment textSegment) {
                this.textSegment = textSegment;
            }

            public TextSegment getTextSegment() {
                return textSegment;
            }

            public void setTextSegment(TextSegment textSegment) {
                this.textSegment = textSegment;
            }

            public static class TextSegment {
                public TextSegment(String content) {
                    this.content = content;
                }

                public String getContent() {
                    return content;
                }

                public void setContent(String content) {
                    this.content = content;
                }

                String content;
            }
            public TextSegment textSegment;
        }


        String displayName;


    }
}
