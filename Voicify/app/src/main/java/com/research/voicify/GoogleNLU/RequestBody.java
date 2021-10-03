package com.research.voicify.GoogleNLU;

import com.google.cloud.automl.v1.ExamplePayload;

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

            public  String content;
        }
    }
}
