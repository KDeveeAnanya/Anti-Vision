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

    // Keep the secret in Render environment variables.
    @Value("${nvidia.api.key}")
    private String nvidiaApiKey;

    // Keep NVIDIA configuration directly here.
    private static final String NVIDIA_API_URL =
            "https://integrate.api.nvidia.com/v1/chat/completions";

        private static final String NVIDIA_MODEL = "openai/gpt-oss-20b";

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
        System.err.println("--- NVIDIA FAILED - USING LOCAL ANALYSIS ---");
        System.err.println(e.getMessage());

        String input = rawInput.trim().toLowerCase();

        String trigger = "A situation that led to the behavior";
        String emotion = "Stress or discomfort";
        String consequence = "The behavior prevented you from focusing on what mattered";

        if (input.contains("scroll") || input.contains("instagram") || input.contains("phone")) {
            trigger = "Using the phone when intending to work or study";
            emotion = "Boredom or avoidance";
            consequence = "Time was lost and the important task was delayed";
        } else if (input.contains("procrast")) {
            trigger = "Facing a task that feels difficult or uncomfortable";
            emotion = "Overwhelm or avoidance";
            consequence = "The task was postponed and pressure increased";
        } else if (input.contains("late") || input.contains("sleep")) {
            trigger = "Staying engaged with activities late at night";
            emotion = "Difficulty disengaging";
            consequence = "Sleep was delayed and the next day was affected";
        }

        String preventiveRule =
                "IF I notice this behavior starting, THEN I will pause and take one small action toward my intended task.";

        String earlyWarning =
                "Notice the first urge to avoid the task or continue the distracting behavior.";

        return new RuleResponse(
                trigger,
                emotion,
                consequence,
                preventiveRule,
                earlyWarning
        );
    }
}

    private RuleResponse callNvidiaApi(String rawInput) throws Exception {

        if (nvidiaApiKey == null
                || nvidiaApiKey.trim().isEmpty()
                || "your_api_key_here".equals(nvidiaApiKey.trim())) {

            throw new IllegalStateException(
                    "NVIDIA_API_KEY is missing or not configured"
            );
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
                "model", NVIDIA_MODEL,
                "messages", List.of(
                        Map.of(
                                "role", "system",
                                "content", systemPrompt
                        ),
                        Map.of(
                                "role", "user",
                                "content", rawInput
                        )
                ),
                "temperature", 0.2,
                "max_tokens", 700,
                "stream", false
        );

        String requestBody =
                objectMapper.writeValueAsString(payload);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NVIDIA_API_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header(
                        "Authorization",
                        "Bearer " + nvidiaApiKey.trim()
                )
                .POST(
                        HttpRequest.BodyPublishers
                                .ofString(requestBody)
                )
                .build();

        System.out.println("--- NVIDIA REQUEST ---");
        System.out.println("Model: " + NVIDIA_MODEL);
        System.out.println("URL: " + NVIDIA_API_URL);

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        System.out.println("--- NVIDIA RESPONSE ---");
        System.out.println("Status Code: " + response.statusCode());
        System.out.println("Response Body: " + response.body());
        System.out.println("-----------------------");

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new RuntimeException(
                    "NVIDIA API returned HTTP "
                            + response.statusCode()
                            + ": "
                            + response.body()
            );
        }

        JsonNode root =
                objectMapper.readTree(response.body());

        JsonNode choices =
                root.path("choices");

        if (!choices.isArray() || choices.isEmpty()) {
            throw new IllegalStateException(
                    "NVIDIA response has no choices"
            );
        }

        JsonNode message =
                choices.get(0).path("message");

        String content =
                message.path("content").asText(null);

        if (content == null || content.isBlank()) {
            throw new IllegalStateException(
                    "NVIDIA response has no message content"
            );
        }

        String json =
                extractJsonObject(content);

        RuleResponse result =
                objectMapper.readValue(
                        json,
                        RuleResponse.class
                );

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
            throw new IllegalStateException(
                    "NVIDIA returned non-JSON content: "
                            + cleaned
            );
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

            throw new IllegalStateException(
                    "NVIDIA returned an incomplete analysis"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}