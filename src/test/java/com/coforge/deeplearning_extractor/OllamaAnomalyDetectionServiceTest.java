package com.coforge.deeplearning_extractor;

import com.coforge.deeplearning_extractor.ollama.OllamaAnomalyDetectionService;
import com.coforge.deeplearning_extractor.ollama.OllamaService;
import com.coforge.deeplearning_extractor.autoencoder.DataRow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
public class OllamaAnomalyDetectionServiceTest {

    @InjectMocks
    private OllamaAnomalyDetectionService service;

    @Mock
    private OllamaService ollamaService;

    @Mock
    private DataRow dataRow;

    @Test
    public void testDetection_withOllamaAvailable() {
    
        when(ollamaService.isOllamaAvailable()).thenReturn(true);
        when(dataRow.getName()).thenReturn("Anamika Sharma");
        when(dataRow.getDate()).thenReturn("1985-01-01");
        when(dataRow.getIata()).thenReturn("US");
        when(dataRow.getSeatNumber()).thenReturn("12A");
        when(dataRow.getCabinClass()).thenReturn("E");
 
        String ollamaResult = """
            {
                "ollamaAnomaly": false,
                "confidence": 0.9,
                "reasoning": "No anomaly detected",
                "concerns": [],
                "recommendation": "ALLOW"
            }
            """;
        when(ollamaService.analyzePassengerData(anyString(), anyString())).thenReturn(ollamaResult);

        // Act
        Map<String, Object> result = service.analyzePassenger(dataRow);

  
        assertNotNull(result);
        assertTrue(result.containsKey("overallAnomaly"));
        assertEquals(false, result.get("overallAnomaly"));
        assertEquals("none", result.get("anomalyType"));
        assertEquals("ALLOW", result.get("recommendation"));
    }

    @Test
    public void testDetection_fallbackToRulesIfOllamaUnavailable() {
     
        when(ollamaService.isOllamaAvailable()).thenReturn(false);
        when(dataRow.getName()).thenReturn("Anamika Sharma");
        when(dataRow.getDate()).thenReturn("1985-01-01");
        when(dataRow.getIata()).thenReturn("US");
        when(dataRow.getSeatNumber()).thenReturn("12A");
        when(dataRow.getCabinClass()).thenReturn("ECONOMY");

        Map<String, Object> result = service.analyzePassenger(dataRow);

        assertNotNull(result);
        assertTrue(result.containsKey("overallAnomaly"));
        assertEquals(false, result.get("overallAnomaly"));
        assertEquals("none", result.get("anomalyType"));
        assertEquals("FALLBACK_TO_RULES", result.get("recommendation"));
    }
}