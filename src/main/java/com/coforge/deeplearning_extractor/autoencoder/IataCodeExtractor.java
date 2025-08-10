package com.coforge.deeplearning_extractor.autoencoder;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

public class IataCodeExtractor {
    private static List<String> cachedIataCodes = null;
    private String filepath;
    private int colIndex;

    public IataCodeExtractor(String filepath, int colIndex) {
        this.filepath = filepath;
        this.colIndex = colIndex;
        
        if (cachedIataCodes == null) {
            cachedIataCodes = loadIataCodes();
        }
    }

    private List<String> loadIataCodes() {
        List<String> codes = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filepath))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                if (colIndex < nextLine.length) {
                    codes.add(nextLine[colIndex]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return codes;
    }

    public List<String> getIataCodes() {
        return cachedIataCodes;
    }
    
    public boolean validIataCode(DataRow row) {
        return cachedIataCodes.contains(row.getIata().toUpperCase());
    }

	public String anomalyType(DataRow row, DL4JAutoencoderModel model) {
		if (!validIataCode(row)) {
			return "Iata code invalid";
		}
		return "valid code";
	}
}