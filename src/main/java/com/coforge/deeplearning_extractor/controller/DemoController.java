package com.coforge.deeplearning_extractor.controller;
	
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
	
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

import com.coforge.deeplearning_extractor.ollama.OllamaAnomalyDetectionService;
import com.coforge.deeplearning_extractor.autoencoder.DataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
	
	
	
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class DemoController {

	@Autowired
    private OllamaAnomalyDetectionService ollamaService;
    
    @PostMapping("/detect")
    public Map<String, Object> detect(@RequestBody DataRow row) {
        // Use the new Ollama-based hybrid detection
        return ollamaService.analyzePassenger(row);
    }
    
    @GetMapping("/ollama/status")
    public Map<String, Object> checkOllamaStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("available", ollamaService.isOllamaAvailable());
        status.put("endpoint", "http://localhost:11434");
        status.put("model", "gemma3:4b");
        status.put("currentUser", "Avaneesh Garg");
        status.put("timestamp", "2025-07-30 05:28:36");
        return status;
    }

    @GetMapping("/ollama/health")
    public Map<String, Object> checkOllamaHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        
        try {
            // Test basic connectivity
            boolean available = ollamaService.isOllamaAvailable();
            health.put("ollamaRunning", available);
            health.put("expectedModel", "gemma3:4b");
            
            if (!available) {
                health.put("suggestions", new String[]{
                    "1. Start Ollama: ollama serve",
                    "2. Check if gemma3:4b is installed: ollama list",
                    "3. Pull model if needed: ollama pull gemma3:4b",
                    "4. Verify port 11434 is accessible"
                });
            }
            
        } catch (Exception e) {
            health.put("error", e.getMessage());
        }
        
        return health;
    }

	@PostMapping("/upload/detect")
	public List<Map<String, Object>> uploadJsonFile(@RequestParam("file") MultipartFile file) {
	    if (file.isEmpty()) {
	        throw new IllegalArgumentException("File is empty!");
	    }
	    try {
	        ObjectMapper objectMapper = new ObjectMapper();
	        DataRow[] dataArray = objectMapper.readValue(file.getInputStream(), DataRow[].class);
	        List<DataRow> rows = List.of(dataArray);
	        List<Map<String, Object>> results = new ArrayList<>();
	        for (DataRow row : rows) {
	            results.add(ollamaService.analyzePassenger(row));
	        }
	        return results;
	    } catch (IOException e) {
	        e.printStackTrace();
	        throw new RuntimeException("Failed to process file: " + e.getMessage());
	    }
    }
		
}
