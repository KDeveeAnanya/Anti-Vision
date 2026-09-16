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
import java.util.List;
import java.util.Map;

@Service
public class RuleEngineService {

    @Value("${nvidia.api.key}")
    private String nvidiaApiKey;

    @Value("${nvidia.api.url:https://integrate.api.nvidia.com/v1/chat/completions}")
    private String nvidiaApiUrl;

    @Value("${nvidia.api.model:meta/llama-3.3-70b-instruct}")
    private String nvidiaModel;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RuleEngineService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    public RuleResponse analyzeInput(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty");
        }

        try {
            return callNvidiaApi(rawInput.trim());
        } catch (Exception e) {
            // Do NOT silently return the old keyword fallback. That made the UI look
            // like the AI worked when the NVIDIA request actually failed.
            System.err.println("--- NVIDIA AI ANALYSIS FAILED ---");
            e.printStackTrace(System.err);
            throw new RuntimeException("NVIDIA AI analysis failed: " + e.getMessage(), e);
        }
    }

    private RuleResponse callNvidiaApi(String rawInput) throws Exception {
        if (nvidiaApiKey == null || nvidiaApiKey.trim().isEmpty()
                || "your_api_key_here".equals(nvidiaApiKey.trim())) {
            throw new IllegalStateException("NVIDIA_API_KEY is missing or not configured");
        }

        String systemPrompt = """
                You are the behavior-analysis engine for Anti-Vision.
                Analyze the user's reflection and return ONLY one valid JSON object.
                Never return markdown, code fences, explanations, or extra text.

                Required string fields:
                - trigger: the specific situation, action, object, person, or context that tends to start the behavior
                - emotion: the feeling immediately before or during the behavior
                - consequence: the concrete negative result that followed
                - preventiveRule: a practical IF/THEN implementation intention that prevents or interrupts the behavior
                - earlyWarning: a short red-flag statement describing what the user should notice early

                Infer the most likely information from the reflection instead of returning "Unknown".
                Keep every field specific to the user's actual reflection.
                preventiveRule MUST start with "IF" and contain "THEN".

                JSON shape:
                {"trigger":"...","emotion":"...","consequence":"...","preventiveRule":"IF ... THEN ...","earlyWarning":"..."}
                """;

        Map<String, Object> payload = Map.of(
                "model", nvidiaModel,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", rawInput)
                ),
                "temperature", 0.2,
                "max_tokens", 700,
                "stream", false
        );

        String requestBody = objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(nvidiaApiUrl))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + nvidiaApiKey.trim())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        System.out.println("--- NVIDIA REQUEST ---");
        System.out.println("Model: " + nvidiaModel);
        System.out.println("URL: " + nvidiaApiUrl);

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("--- NVIDIA RESPONSE ---");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("-----------------------");

        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new RuntimeException(
                    "NVIDIA API returned HTTP " + response.statusCode() + ": " + response.body()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode choices = root.path("choices");

        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException("NVIDIA response has no choices");
        }

        JsonNode message = choices.get(0).path("message");
        String content = message.path("content").asText(null);

        if (content == null || content.isBlank()) {
            throw new IllegalStateException("NVIDIA response has no message content");
        }

        String json = extractJsonObject(content);
        RuleResponse result = objectMapper.readValue(json, RuleResponse.class);
        validateResult(result);

        return result;
    }

    private String extractJsonObject(String content) {
        String cleaned = content
                .replace("```json", "")
                .replace("```JSON", "")
                .replace("```", "")
                .trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalStateException("NVIDIA returned non-JSON content: " + cleaned);
        }

        return cleaned.substring(start, end + 1);
    }

    private void validateResult(RuleResponse result) {
        if (result == null
                || isBlank(result.getTrigger())
                || isBlank(result.getEmotion())
                || isBlank(result.getConsequence())
                || isBlank(result.getPreventiveRule())
                || isBlank(result.getEarlyWarning())) {
            throw new IllegalStateException("NVIDIA returned an incomplete analysis");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
