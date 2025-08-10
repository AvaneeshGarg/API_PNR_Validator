package com.coforge.deeplearning_extractor.ollama;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.coforge.deeplearning_extractor.autoencoder.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Arrays;

@Service
public class OllamaAnomalyDetectionService {
    
    @Autowired
    private OllamaService ollamaService;
    
    private final IataCodeExtractor iataExtractor;
    private final ObjectMapper objectMapper;
    
    public OllamaAnomalyDetectionService() {
        this.iataExtractor = new IataCodeExtractor("src/main/resources/countries.csv", 1);
        this.objectMapper = new ObjectMapper();
    }
    
    public Map<String, Object> analyzePassenger(DataRow passengerData) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        try {
            Map<String, Object> ruleResults = performRuleBasedAnalysis(passengerData);
            
            Map<String, Object> ollamaResults = new HashMap<>();
            if (ollamaService.isOllamaAvailable()) {
                ollamaResults = performOllamaAnalysis(passengerData, ruleResults);
            } else {
                ollamaResults.put("ollamaAnomaly", false);
                ollamaResults.put("confidence", 0.0);
                ollamaResults.put("reasoning", "Ollama service not available");
                ollamaResults.put("concerns", new String[]{});
                ollamaResults.put("recommendation", "FALLBACK_TO_RULES");
            }
            
            result = combineAnalyses(passengerData, ruleResults, ollamaResults);
            
        } catch (Exception e) {
            result.put("error", "Analysis failed: " + e.getMessage());
            result.put("fallbackToRules", true);
        }
        
        return result;
    }
    
    private Map<String, Object> performRuleBasedAnalysis(DataRow row) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        boolean dateInvalid = DateExtractor.isInvalidDate(row.getDate());
        boolean nameInvalid = NameExtractor.isNameInvalid(row.getName());
        boolean ageInvalid = isAgeInvalid(row);
        boolean cabinInvalid = isCabinClassInvalid(row.getCabinClass());
        boolean iataInvalid = !iataExtractor.validIataCode(row);
        
        boolean ruleAnomaly = dateInvalid || nameInvalid || ageInvalid || cabinInvalid || iataInvalid;
        
        result.put("ruleBasedAnomaly", ruleAnomaly);
        result.put("dateInvalid", dateInvalid);
        result.put("nameInvalid", nameInvalid);
        result.put("ageInvalid", ageInvalid);
        result.put("cabinInvalid", cabinInvalid);
        result.put("iataInvalid", iataInvalid);
        result.put("age", FeatureExtractor.calculateAge(row));
        
        return result;
    }
    
    private Map<String, Object> performOllamaAnalysis(DataRow passengerData, 
                                                      Map<String, Object> ruleResults) {
        try {
            String passengerJson = objectMapper.writeValueAsString(Map.of(
                "name", passengerData.getName(),
                "birthDate", passengerData.getDate(),
                "iataCode", passengerData.getIata(),
                "seatNumber", passengerData.getSeatNumber(),
                "cabinClass", passengerData.getCabinClass(),
                "calculatedAge", FeatureExtractor.calculateAge(passengerData)
            ));
            
            String extractorResults = objectMapper.writeValueAsString(Map.of(
                "ruleBasedResults", ruleResults
            ));
            
            String ollamaResponse = ollamaService.analyzePassengerData(passengerJson, extractorResults);
            
            System.out.println("DEBUG: Ollama raw response: " + ollamaResponse);
            
            JsonNode ollamaJson = objectMapper.readTree(ollamaResponse);
            
            Map<String, Object> result = new LinkedHashMap<>();
            
            result.put("ollamaAnomaly", ollamaJson.has("ollamaAnomaly") ? ollamaJson.get("ollamaAnomaly").asBoolean(false) : false);
            result.put("confidence", ollamaJson.has("confidence") ? ollamaJson.get("confidence").asDouble(0.0) : 0.0);
            result.put("reasoning", ollamaJson.has("reasoning") ? ollamaJson.get("reasoning").asText("No reasoning provided") : "No reasoning provided");
            
            String[] concerns = new String[]{};
            if (ollamaJson.has("concerns") && ollamaJson.get("concerns").isArray()) {
                JsonNode concernsNode = ollamaJson.get("concerns");
                concerns = new String[concernsNode.size()];
                for (int i = 0; i < concernsNode.size(); i++) {
                    concerns[i] = concernsNode.get(i).asText();
                }
            }
            result.put("concerns", concerns);
            result.put("recommendation", ollamaJson.has("recommendation") ? ollamaJson.get("recommendation").asText("ALLOW") : "ALLOW");
            
            return result;
            
        } catch (Exception e) {
            Map<String, Object> errorResult = new LinkedHashMap<>();
            errorResult.put("ollamaAnomaly", false);
            errorResult.put("confidence", 0.0);
            errorResult.put("reasoning", "Failed to analyze with Ollama: " + e.getMessage());
            errorResult.put("concerns", new String[]{});
            errorResult.put("recommendation", "FALLBACK_TO_RULES");
            return errorResult;
        }
    }
    
    private Map<String, Object> combineAnalyses(DataRow passengerData,
                                                Map<String, Object> ruleResults,
                                                Map<String, Object> ollamaResults) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        result.put("NAME", passengerData.getName());
        result.put("DATE", passengerData.getDate());
        result.put("IATA_code", passengerData.getIata());
        result.put("Age", ruleResults.get("age"));
        result.put("SeatNumber", passengerData.getSeatNumber());
        result.put("CabinClass", passengerData.getCabinClass());
        result.put("currentDateTime", "2025-07-30 05:28:36");
        
        result.put("ruleBasedAnomaly", ruleResults.get("ruleBasedAnomaly"));
        result.put("ollamaAnomaly", ollamaResults.get("ollamaAnomaly"));
        
        boolean isAnomaly = determineOverallAnomaly(ruleResults, ollamaResults);
        String anomalyType = determineAnomalyType(ruleResults, ollamaResults);
        
        result.put("overallAnomaly", isAnomaly);
        result.put("anomalyType", anomalyType);
        result.put("confidence", ollamaResults.get("confidence"));
        result.put("aiReasoning", ollamaResults.get("reasoning"));
        result.put("aiConcerns", ollamaResults.get("concerns"));
        result.put("recommendation", ollamaResults.get("recommendation"));
        
        result.put("IATA_valid", !((Boolean) ruleResults.get("iataInvalid")));
        
        return result;
    }
    
    private boolean determineOverallAnomaly(Map<String, Object> ruleResults,
                                          Map<String, Object> ollamaResults) {
        boolean ruleAnomaly = (Boolean) ruleResults.get("ruleBasedAnomaly");
        boolean ollamaAnomaly = (Boolean) ollamaResults.get("ollamaAnomaly");
        double ollamaConfidence = (Double) ollamaResults.get("confidence");
        
        if (ruleAnomaly) return true;
        
        if (ollamaAnomaly && ollamaConfidence > 0.7) return true;
        
        if (ollamaAnomaly && ollamaConfidence > 0.5) return true;
        
        return false;
    }
    
    private String determineAnomalyType(Map<String, Object> ruleResults,
                                       Map<String, Object> ollamaResults) {
        if ((Boolean) ruleResults.get("dateInvalid")) return "invalid_date";
        if ((Boolean) ruleResults.get("nameInvalid")) return "invalid_name";
        if ((Boolean) ruleResults.get("ageInvalid")) return "invalid_age";
        if ((Boolean) ruleResults.get("cabinInvalid")) return "invalid_cabin";
        if ((Boolean) ruleResults.get("iataInvalid")) return "invalid_iata";
        
        if ((Boolean) ollamaResults.get("ollamaAnomaly")) {
            return "ai_detected_anomaly";
        }
        
        return "none";
    }
    
    private boolean isAgeInvalid(DataRow row) {
        int age = FeatureExtractor.calculateAge(row);
        return age < 12 || age > 100;
    }
    
    private boolean isCabinClassInvalid(String cabinClass) {
        if (cabinClass == null || cabinClass.trim().isEmpty()) return true;
        String[] validClasses = {"A", "B", "C", "D", "E", "F", "ECONOMY", "BUSINESS", "FIRST"};
        String upper = cabinClass.toUpperCase().trim();
        return !Arrays.asList(validClasses).contains(upper);
    }

    public boolean isOllamaAvailable() {
        return ollamaService.isOllamaAvailable();
    }
}
