package com.example.mybudget1;

import java.util.List;

public class GeminiRequest {
    private String model = "gemini-chat";
    private String prompt;
    private int max_tokens = 500;

    public GeminiRequest(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
    public int getMax_tokens() {
        return max_tokens;
    }
}

class GeminiResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public static class Candidate {
        private Content content;

        public Content getContent() {
            return content;
        }
    }

    public static class Content {
        private List<Part> parts;

        public List<Part> getParts() {
            return parts;
        }
    }

    public static class Part {
        private String text;

        public String getText() {
            return text;
        }
    }
}

