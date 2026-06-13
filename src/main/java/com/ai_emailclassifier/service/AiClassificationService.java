package com.ai_emailclassifier.service;

import com.ai_emailclassifier.entity.EmailCategory;
import com.ai_emailclassifier.exception.AiServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/*
 * This class ONLY knows about talking to the AI API.
 * EmailService doesn't care HOW classification works — just that it does.
 * PROMPT ENGINEERING STRATEGY:
 * - We use a structured JSON prompt to force a machine-parseable response
 * - Few-shot examples would improve accuracy but increase token usage
 * - We constrain output to exactly 4 labels to prevent hallucination
 */
@Service
@Slf4j
public class AiClassificationService {

    @Value("${ai.openrouter.api-key}")
    private String apiKey;

    @Value("${ai.openrouter.base-url}")
    private String baseUrl;

    @Value("${ai.openrouter.model}")
    private String model;

    @Value("${ai.openrouter.max-retries}")
    private int maxRetries;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiClassificationService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    //Result record — clean, immutable data carrier
    public record ClassificationResult(
            EmailCategory label,
            double confidenceScore
    ) {}


   // WHY retry logic? AI APIs can have transient failures.
   //  A simple retry with exponential backoff handles 90% of transient issues.

    public ClassificationResult classify(String subject, String body) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                log.info("Classifying email. Attempt {}/{}",
                        attempt + 1, maxRetries);
                return callOpenRouterApi(subject, body);
            } catch (AiServiceException e) {
                lastException = e;
                attempt++;
                if (attempt < maxRetries) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000; // Exponential backoff
                    log.warn("AI API call failed. Retrying in {}ms. Reason: {}", backoffMs, e.getMessage());
                    try {
                        Thread.sleep(backoffMs); // 2s, 4s, 8s...
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("All {} AI API attempts failed.", maxRetries);
        throw new AiServiceException("AI classification failed after " + maxRetries + " attempts", lastException);
    }

    private ClassificationResult callOpenRouterApi(String subject, String body) {
        HttpHeaders headers = buildHeaders();
        String requestBody = buildRequestBody(subject, body);

        HttpEntity<String> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl, HttpMethod.POST, request, String.class);

            return parseResponse(response.getBody());

        } catch (HttpClientErrorException.TooManyRequests e) {
            //WHY specific catch - Rate limit = retry won't help immediately
            log.error("OpenRouter rate limit hit: {}", e.getMessage());
            throw new AiServiceException("AI API rate limit exceeded. Try again later.");
        } catch (HttpClientErrorException.Unauthorized e) {
            log.error("OpenRouter API key invalid");
            throw new AiServiceException("AI API authentication failed. Check API key.");
        } catch (ResourceAccessException e) {
            //Network timeout or connection refused
            log.error("AI API timeout or connection refused: {}", e.getMessage());
            throw new AiServiceException("AI API connection failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected AI API error: {}", e.getMessage());
            throw new AiServiceException("Unexpected AI error: " + e.getMessage(), e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //OpenRouter follows OAuth 2.0 Bearer token standard
        headers.set("Authorization", "Bearer " + apiKey);
        // OpenRouter requires this for free tier identification
        headers.set("HTTP-Referer", "https://email-classifier-api.com");
        headers.set("X-Title", "Email Classifier");
        return headers;
    }

    /*
     * This is the core of the AI integration
     * - system = sets AI persona and strict output constraints
     * - user = the actual email content
     * JSON output format -Forces deterministic, parseable output. Free-text responses are unreliable.
     * WHY explicit label list?
     * Prevents the model from inventing new categories (hallucination prevention).
     * confidence instruction :-
     * Gives us a quality signal — low confidence = uncertain classification.
     */
    private String buildRequestBody(String subject, String body) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            root.put("model", model);
            root.put("max_tokens", 150); //JSON response is small — no need for more

            ArrayNode messages = objectMapper.createArrayNode();

            // System message — strict persona and output format
            ObjectNode systemMsg = objectMapper.createObjectNode();
            systemMsg.put("role", "system");
            systemMsg.put("content", """
                You are an expert email classification system.
                Classify the given email into EXACTLY ONE of these categories:
                - SPAM: Unsolicited, scam, phishing, or junk emails
                - IMPORTANT: Work, financial, legal, personal or urgent emails
                - PROMOTIONS: Marketing, sales, deals, newsletters from businesses
                - SOCIAL: Social media, friend/family messages, events
                
                Respond ONLY with a valid JSON object in this exact format:
                {"classification": "CATEGORY", "confidence": 0.00}
                
                Rules:
                - classification must be one of: SPAM, IMPORTANT, PROMOTIONS, SOCIAL
                - confidence must be a decimal between 0.0 and 1.0
                - Do NOT include any other text, explanation, or markdown
                """);
            messages.add(systemMsg);

            //User message — the actual email content
            ObjectNode userMsg = objectMapper.createObjectNode();
            userMsg.put("role", "user");
            userMsg.put("content", String.format(
                    "Classify this email:\n\nSubject: %s\n\nBody: %s",
                    subject,
                    //using substring to Prevent sending massive bodies to AI (saves tokens/cost)
                    body.length() > 2000 ? body.substring(0, 2000) + "..." : body
            ));
            messages.add(userMsg);

            root.set("messages", messages);
            return objectMapper.writeValueAsString(root);

        } catch (Exception e) {
            throw new AiServiceException("Failed to build AI request: " + e.getMessage());
        }
    }

    /*
     * Why strict parsing?
     * AI models sometimes add extra text even when told not to.
     * We extract JSON defensively — find the { } block within any surrounding text.
     */
    private ClassificationResult parseResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            //navigating to OpenRouter's response structure
            String content = root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.debug("Raw AI response content: {}", content);

            //Removes surrounding whitespace/newlines from AI response
            content = content.trim();

            //Extracting JSON even if model added extra text
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start == -1 || end == -1) {
                throw new AiServiceException("AI response did not contain valid JSON: " + content);
            }
            content = content.substring(start, end + 1);

            JsonNode parsed = objectMapper.readTree(content);
            String classification = parsed.path("classification")
                    .asText("IMPORTANT")
                    .trim()
                    .toUpperCase();
            double confidence = parsed.path("confidence").asDouble(0.75);

            // validating label to guard against model returning unexpected values
            EmailCategory category;
            try {
                category = EmailCategory.valueOf(classification);
            } catch (IllegalArgumentException ex) {
                log.warn(
                        "AI returned unknown classification '{}', defaulting to IMPORTANT",
                        classification
                );
                category = EmailCategory.IMPORTANT;
            }
            log.info("AI classified email as: {} (confidence: {})", category, confidence);
            return new ClassificationResult(category, confidence);

        } catch (AiServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", e.getMessage());
            throw new AiServiceException("Failed to parse AI response: " + e.getMessage(), e);
        }
    }

}