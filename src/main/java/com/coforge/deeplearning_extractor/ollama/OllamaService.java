package com.coforge.deeplearning_extractor.ollama;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.time.Duration;

@Service
public class OllamaService {
    
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    @Value("${ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    public OllamaService() {
        this.webClient = WebClient.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public boolean isOllamaAvailable() {
        try {
            String response = webClient.get()
                .uri(ollamaBaseUrl + "/api/tags")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .block();
            
            // Check if gemma3:4b model is available
            boolean hasModel = response != null && response.contains("gemma3:4b");
            System.out.println("Ollama available: " + (response != null) + ", has gemma3:4b: " + hasModel);
            
            return hasModel;
        } catch (Exception e) {
            System.err.println("Ollama not available: " + e.getMessage());
            return false;
        }
    }
    
    public String analyzePassengerData(String passengerJson, String extractorResults) {
        try {
            // Create a detailed prompt for anomaly detection
            String prompt = buildAnalysisPrompt(passengerJson, extractorResults);
            
            Map<String, Object> requestBody = Map.of(
                "model", "gemma3:4b", // Using your local model
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "options", Map.of(
                    "temperature", 0.1, // Lower temperature for more consistent responses
                    "top_p", 0.8,
                    "num_predict", 200  // Limit response length for faster processing
                )
            );
            
            String response = webClient.post()
                .uri(ollamaBaseUrl + "/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(120)) // 2 minute timeout for gemma3:4b
                .block();
            
            // Parse Ollama response to extract the actual generated content
            JsonNode responseJson = objectMapper.readTree(response);
            String generatedText = responseJson.get("response").asText();
            
            // Try to parse the generated JSON, if it fails, create a fallback response
            try {
                objectMapper.readTree(generatedText); // Validate JSON format
                return generatedText;
            } catch (Exception e) {
                // If the AI didn't return valid JSON, create a structured response
                return createFallbackResponse(generatedText, extractorResults);
            }
            
        } catch (WebClientResponseException e) {
            System.err.println("Ollama API error: " + e.getMessage());
            return createErrorResponse("Ollama API error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Failed to analyze with Ollama: " + e.getMessage());
            return createErrorResponse("Analysis failed: " + e.getMessage());
        }
    }
    
    private String buildAnalysisPrompt(String passengerData, String extractorResults) {
        return String.format("""
            You are an expert airline security analyst. Analyze this passenger data for anomalies.
            
            Passenger Data: %s
            Rule-based Analysis: %s
            
            IMPORTANT: Consider cultural diversity - names like Anamika, Kumar, Zhang, Mohammed, etc. are legitimate names from different cultures.
            
            Focus on:
            - Obviously fake names (Test User, John Doe, Admin, etc.)
            - Invalid date formats or impossible dates
            - Invalid country codes or geographic inconsistencies
            - Suspicious booking patterns or data combinations
            - Technical data corruption or formatting issues
            
            DO NOT flag legitimate names from different cultures as suspicious.
            
            You MUST respond with ONLY valid JSON in this exact format (no additional text):
            {
              "ollamaAnomaly": false,
              "confidence": 0.0,
              "reasoning": "Normal passenger data with legitimate international name",
              "concerns": [],
              "recommendation": "ALLOW"
            }
            """, passengerData, extractorResults);
    }
    
    private String createFallbackResponse(String aiResponse, String extractorResults) {
        try {
            // Parse extractor results to make a basic decision
            JsonNode extractorJson = objectMapper.readTree(extractorResults);
            boolean hasRuleViolations = extractorJson.get("ruleBasedResults").get("ruleBasedAnomaly").asBoolean();
            
            Map<String, Object> fallback = Map.of(
                "ollamaAnomaly", hasRuleViolations,
                "confidence", hasRuleViolations ? 0.8 : 0.6,
                "reasoning", "AI analysis: " + aiResponse.substring(0, Math.min(200, aiResponse.length())),
                "concerns", hasRuleViolations ? new String[]{"rule_violations_detected"} : new String[]{"pattern_analysis_completed"},
                "recommendation", hasRuleViolations ? "INVESTIGATE" : "ALLOW"
            );
            
            return objectMapper.writeValueAsString(fallback);
        } catch (Exception e) {
            return createErrorResponse("Failed to create fallback response");
        }
    }
    
    private String createErrorResponse(String errorMessage) {
        try {
            Map<String, Object> error = Map.of(
                "ollamaAnomaly", false,
                "confidence", 0.0,
                "reasoning", errorMessage,
                "concerns", new String[]{},
                "recommendation", "FALLBACK_TO_RULES"
            );
            return objectMapper.writeValueAsString(error);
        } catch (Exception e) {
            // Last resort fallback
            return "{\"ollamaAnomaly\":false,\"confidence\":0.0,\"reasoning\":\"System error\",\"concerns\":[],\"recommendation\":\"FALLBACK_TO_RULES\"}";
        }
    }
}
