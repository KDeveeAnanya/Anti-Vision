package com.antivision.services;

import com.antivision.dto.RuleResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service class responsible for the core logic of the Anti-Vision system.
 * It analyzes raw user input to extract behavioral patterns using the Gemini API,
 * with a fallback to basic keyword matching.
 */
@Service
public class RuleEngineService {

    /**
     * @Value tells Spring to look into application.yml for a property named 'gemini.api.key'
     * and inject its value into this String variable.
     */
    @Value("${nvidia.api.key}")
    private String nvidiaApiKey;

    @Value("${nvidia.api.url}")
    private String nvidiaApiUrl;

    // Jackson's ObjectMapper is the standard Java library for converting Objects to/from JSON.
    private final ObjectMapper objectMapper;
    // Java 11+ built-in modern HTTP client for making API requests.
    private final HttpClient httpClient;

    // Hardcoded keywords for the fallback mechanism
    private static final List<String> TRIGGER_KEYWORDS = Arrays.asList("coffee", "late", "phone", "scrolling", "sugar", "alcohol", "tired");
    private static final List<String> EMOTION_KEYWORDS = Arrays.asList("anxious", "sad", "angry", "stressed", "overwhelmed", "guilty");
    private static final List<String> CONSEQUENCE_KEYWORDS = Arrays.asList("ruined", "stayed up", "missed", "failed", "headache", "brain fog", "wasted");

    public RuleEngineService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        // We initialize the HttpClient once to reuse connections efficiently
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public RuleResponse analyzeInput(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty");
        }

        try {
            return callNvidiaApi(rawInput);
        } catch (Exception e) {
            System.err.println("--- DEBUG: CAUGHT EXCEPTION ---");
            e.printStackTrace(System.err);
            System.err.println("--- DEBUG: FALLING BACK TO KEYWORD MATCHER ---");
            return fallbackKeywordMatcher(rawInput);
        }
    }

    private RuleResponse callNvidiaApi(String rawInput) throws Exception {
        System.out.println("--- DEBUG: API KEY LOADED ---");
        if (nvidiaApiKey == null || nvidiaApiKey.isEmpty()) {
            System.out.println("API Key is NULL or EMPTY");
        } else {
            int len = Math.min(nvidiaApiKey.length(), 5);
            System.out.println("API Key starts with: " + nvidiaApiKey.substring(0, len) + "...");
        }

        if ("your_api_key_here".equals(nvidiaApiKey)) {
            throw new IllegalStateException("API key not configured. Using fallback.");
        }

        String systemPrompt = "You are a psychological behavior analyzer. Extract the following from the user's input: " +
                "trigger, emotion, consequence, preventiveRule (an IF/THEN implementation intention), and earlyWarning (identifying the emotion as a red flag). " +
                "Return ONLY a pure JSON object with exactly those 5 fields as string values. Do not include markdown formatting, backticks, or any conversational text.";

        // Construct the NVIDIA API payload (OpenAI format)
        Map<String, Object> payload = Map.of(
                "model", "meta/llama-3.1-8b-instruct",
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", rawInput)
                ),
                "temperature", 0.2,
                "max_tokens", 1024,
                "stream", false
        );

        // Convert the Java Map into a JSON String
        String requestBody = objectMapper.writeValueAsString(payload);

        // Build the HTTP POST request with headers
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(nvidiaApiUrl))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + nvidiaApiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request synchronously
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("--- DEBUG: RAW NVIDIA API RESPONSE ---");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("--------------------------------------");

        if (response.statusCode() != 200) {
            throw new RuntimeException("NVIDIA API error: " + response.body());
        }

        // Parse the JSON response wrapper that NVIDIA returns (OpenAI format)
        JsonNode rootNode = objectMapper.readTree(response.body());
        String textResponse = rootNode.path("choices").get(0).path("message").path("content").asText();
        
        // Safety check: Clean up any potential markdown if the model disobeys the "no formatting" instruction
        textResponse = textResponse.replaceAll("```json", "").replaceAll("```", "").trim();

        // Jackson Magic: Deserialize the clean JSON string directly into our RuleResponse DTO
        return objectMapper.readValue(textResponse, RuleResponse.class);
    }

    private RuleResponse fallbackKeywordMatcher(String rawInput) {
        String lowerInput = rawInput.toLowerCase();

        String trigger = extractKeyword(lowerInput, TRIGGER_KEYWORDS, "Unknown Trigger");
        String emotion = extractKeyword(lowerInput, EMOTION_KEYWORDS, "Unknown Emotion");
        String consequence = extractKeyword(lowerInput, CONSEQUENCE_KEYWORDS, "Unknown Consequence");

        String preventiveRule = generatePreventiveRule(trigger, consequence);
        String earlyWarning = generateEarlyWarning(emotion, trigger);

        return new RuleResponse(trigger, emotion, consequence, preventiveRule, earlyWarning);
    }

    private String extractKeyword(String input, List<String> dictionary, String defaultResult) {
        for (String keyword : dictionary) {
            if (input.contains(keyword)) {
                return keyword;
            }
        }
        return defaultResult;
    }

    private String generatePreventiveRule(String trigger, String consequence) {
        if ("Unknown Trigger".equals(trigger)) {
            return "Need more specific trigger to generate a rule.";
        }
        return "IF I am tempted by [" + trigger + "], THEN I will immediately step away for 5 minutes to avoid [" + consequence + "].";
    }

    private String generateEarlyWarning(String emotion, String trigger) {
        if ("Unknown Emotion".equals(emotion)) {
            return "Try to identify how you felt right before this happened.";
        }
        return "Red Flag: When I feel [" + emotion + "], my brain will likely seek out [" + trigger + "] as a coping mechanism.";
    }
}
