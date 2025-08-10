package com.coforge.deeplearning_extractor.autoencoder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.*;

public class NameExtractor {
	
	public static String namePattern = "^[A-Za-z]+([ '-][A-Za-z]+)*$";
	public String datePattern = "^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
	
	static Pattern nameValid = Pattern.compile(namePattern);
	Pattern dateValid = Pattern.compile(datePattern);

	public static boolean isNameInvalid(String name) {
	    if (name == null || name.trim().isEmpty()) {
	        return true;
	    }
	    
	    String trimmedName = name.trim();
	    
	    // Check minimum length (at least 2 characters)
	    if (trimmedName.length() < 2) {
	        return true;
	    }
	    
	    // Allow letters, spaces, hyphens, apostrophes, and dots
	    // This pattern allows international names including Indian names like "Anamika"
	    if (!trimmedName.matches("^[a-zA-Z\\s\\-'.]+$")) {
	        return true;
	    }
	    
	    // Check for excessive repeated characters (like "aaaa" or "xxxx")
	    if (trimmedName.matches(".*(.)\\1{3,}.*")) {
	        return true;
	    }
	    
	    // Check for obviously fake patterns like "Test User", "John Doe", etc.
	    String upperName = trimmedName.toUpperCase();
	    String[] suspiciousNames = {
	        "TEST USER", "TEST TEST", "JOHN DOE", "JANE DOE", 
	        "ADMIN", "USER", "NULL", "UNDEFINED", "EXAMPLE"
	    };
	    
	    for (String suspicious : suspiciousNames) {
	        if (upperName.equals(suspicious)) {
	            return true;
	        }
	    }
	    
	    // Valid name (including "Anamika" and other international names)
	    return false;
	}
	
	public static boolean isInvalidDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return true;
        }
    }
	

	public static String anomalyType(DataRow row, DL4JAutoencoderModel model) {
		boolean invalidName = isNameInvalid(row.getName());
		
		double[] features = FeatureExtractor.getFeatures(row);
		
		boolean specialSymbols = FeatureExtractor.nameHasSpecialSymbols(row);
		boolean capitalLetterInBetween = FeatureExtractor.hasCapInBetween(row);
		boolean repeatedLetters = FeatureExtractor.hasRepeatedLetters(row);
		boolean repeatedWords = FeatureExtractor.hasRepeatedWords(row);
		
		if (invalidName) return "name invalid";
		if (repeatedWords) {return "field(s) contains repeated string(s)";}
		if (specialSymbols) {return "field(s) contains special symbols";}
		if (capitalLetterInBetween) {return "Capital letter in Between";}
		if (repeatedLetters) {return "repeated letters in name";}

		
		
		
		
		return "none";
	}

}
