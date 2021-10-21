package com.research.voicify.GoogleNLU;

import com.google.cloud.automl.v1.ExamplePayload;

/**
 * This is a Java object class which defines the format of request body in
 * restAPI connection to the AutoML server.
 */
public class RequestBody {

    public Payload payload;

    public RequestBody(Payload payload) {
        this.payload = payload;
    }

    public static class Payload {
        public TextSnippet textSnippet;

        public Payload(TextSnippet textSnippet) {
            this.textSnippet = textSnippet;
        }

        public static class TextSnippet{
            public TextSnippet(String content) {
                this.content = content;
            }

            public  String content;     // the actual string content within the payload.
        }
    }
}
