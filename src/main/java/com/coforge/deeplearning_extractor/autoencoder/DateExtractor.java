package com.coforge.deeplearning_extractor.autoencoder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DateExtractor {
	
	public static boolean isInvalidDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return date.isAfter(LocalDate.now());
        } catch (DateTimeParseException e) {
            return true;
        }
    }
		
	public static boolean dateHasSymbols(DataRow row) {
	    String date = row.getDate();
	    if (date == null || date.isEmpty()) return false;
	    for (char c : date.toCharArray()) {
	        if (!Character.isDigit(c) && c != '-') {
	            return true;
	        }
	    }
	    return false;
	}
	
	public static String anomalyType(DataRow row, DL4JAutoencoderModel model) {
		// TODO Auto-generated method stub
		
		boolean dateTooOld = FeatureExtractor.ageTooOld(row);
		boolean dateTooNew = FeatureExtractor.ageTooYoung(row);
		
		if (dateHasSymbols(row)) {return "Date contains special symbols";}
		
		LocalDate birthDate;
        try {
            birthDate = LocalDate.parse(row.getDate());
        } catch (DateTimeParseException e) {
            return "BirthDate is not a valid date format";
        }
        
        if (birthDate.isAfter(LocalDate.now())) {
            return "Future date";
        }
        
		if (dateTooOld) {return "BirthDate seems invalid. Age is too old";}
		if (dateTooNew) {return "BirthDate invalid. Age is too new.";}
		
		
		
		return "none";
	}
}
