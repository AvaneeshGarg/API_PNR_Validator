package com.coforge.deeplearning_extractor.autoencoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CsvDataLoader {
    
    public static List<DataRow> loadDataFromCsv(String csvFilePath) throws IOException {
        List<DataRow> dataRows = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            boolean isFirstLine = true;
            
            while ((line = reader.readLine()) != null) {
                // Skip header line
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }
                
                // Parse CSV line
                String[] values = parseCsvLine(line);
                if (values.length >= 5) {
                    String name = values[0].trim();
                    String date = values[1].trim();
                    String iata = values[2].trim();
                    String seatNumber = values[3].trim();
                    String cabinClass = values[4].trim();
                    
                    dataRows.add(new DataRow(name, date, iata, seatNumber, cabinClass));
                }
            }
        }
        
        return dataRows;
    }
    
    /**
     * Simple CSV parser that handles comma-separated values
     * For more complex CSV parsing, consider using a library like OpenCSV
     */
    private static String[] parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentValue = new StringBuilder();
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"' && (i == 0 || line.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(currentValue.toString());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        // Add the last value
        values.add(currentValue.toString());
        
        return values.toArray(new String[0]);
    }
    
    /**
     * Load a subset of data for training (e.g., first N rows)
     */
    public static List<DataRow> loadTrainingData(String csvFilePath, int maxRows) throws IOException {
        List<DataRow> allData = loadDataFromCsv(csvFilePath);
        if (maxRows <= 0 || maxRows >= allData.size()) {
            return allData;
        }
        return allData.subList(0, Math.min(maxRows, allData.size()));
    }
    
    /**
     * Load data and filter out obvious anomalies for training on "normal" data
     */
    public static List<DataRow> loadNormalDataForTraining(String csvFilePath) throws IOException {
        List<DataRow> allData = loadDataFromCsv(csvFilePath);
        List<DataRow> normalData = new ArrayList<>();
        
        for (DataRow row : allData) {
            // Filter out obvious anomalies based on basic rules
            if (isLikelyNormalData(row)) {
                normalData.add(row);
            }
        }
        
        return normalData;
    }
    
    /**
     * Basic heuristic to identify likely normal data for training
     */
    private static boolean isLikelyNormalData(DataRow row) {
        // Basic validation checks
        if (row.getName() == null || row.getName().trim().isEmpty()) return false;
        if (row.getDate() == null || row.getDate().trim().isEmpty()) return false;
        if (row.getIata() == null || row.getIata().trim().isEmpty()) return false;
        if (row.getSeatNumber() == null || row.getSeatNumber().trim().isEmpty()) return false;
        if (row.getCabinClass() == null || row.getCabinClass().trim().isEmpty()) return false;
        
        // Name should be reasonable length and contain only letters, spaces, hyphens, apostrophes
        String name = row.getName().trim();
        if (name.length() < 2 || name.length() > 50) return false;
        if (!name.matches("^[a-zA-Z\\s\\-']+$")) return false;
        
        // Date should follow YYYY-MM-DD format
        if (!row.getDate().matches("^\\d{4}-\\d{2}-\\d{2}$")) return false;
        
        // IATA code should be 2 characters
        if (row.getIata().length() != 2) return false;
        
        // Seat number should be reasonable format (number + letter)
        if (!row.getSeatNumber().matches("^\\d{1,2}[A-Z]$")) return false;
        
        // Cabin class should be single letter
        if (row.getCabinClass().length() != 1) return false;
        
        return true;
    }
}
