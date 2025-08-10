package com.coforge.deeplearning_extractor.autoencoder;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class FeatureExtractor {
	
	private static final IataCodeExtractor iataExtractor = new IataCodeExtractor("src/main/resources/countries.csv", 1);
	
	public static double[] getFeatures(DataRow row) {
    double nameLen = row.getName().length();
    double specialSymbolCount = countSpecialSymbols(row.getName());
    double hasCapInBetween = hasCapInBetween(row) ? 1.0 : 0.0;
    double hasRepeatedLetters = hasRepeatedLetters(row) ? 1.0 : 0.0;
    double hasRepeatedWords = hasRepeatedWords(row) ? 1.0 : 0.0;
    double age = calculateAge(row);
    double ageTooYoung = ageTooYoung(row) ? 1.0 : 0.0;
    double ageTooOld = ageTooOld(row) ? 1.0 : 0.0;
    double isFuture = DateExtractor.isInvalidDate(row.getDate()) ? 1.0 : 0.0;
    double seatLen = row.getSeatNumber().length();
    double seatAlphaNum = row.getSeatNumber().matches(".*[a-zA-Z].*") && row.getSeatNumber().matches(".*\\d.*") ? 1.0 : 0.0;
    double cabinClassValid = !row.getCabinClass().isEmpty() && row.getCabinClass().length() == 1 && Character.isLetter(row.getCabinClass().charAt(0)) ? 1.0 : 0.0;
	double iataValid = iataExtractor.validIataCode(row) ? 1.0 : 0.0;

    return new double[] {
        nameLen, specialSymbolCount, hasCapInBetween, hasRepeatedLetters, hasRepeatedWords,
        age, ageTooYoung, ageTooOld, isFuture, seatLen, seatAlphaNum, cabinClassValid, iataValid
    };
}
	
	public static boolean nameHasSpecialSymbols(DataRow row) {
        for (char c : row.getName().toCharArray()) {
            if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                return true;
            }
        }
        return false;
    }

	public static int countSpecialSymbols(String name) {
        int count = 0;
        for (char c : name.toCharArray()) {
            if (!Character.isLetter(c) && c != ' ' && c != '-' && c != '\'') {
                count++;
            }
        }
        return count;
    }

	public static boolean hasCapInBetween(DataRow row) {
		// TODO Auto-generated method stub
		String name = row.getName();
		if (name == null || name.isEmpty()) {return false;}
		
		for (int i = 1; i<name.length(); i++) {
			if (Character.isUpperCase(name.charAt(i))){
				return true;
			}
		}
        return false;
	}

	public static boolean hasRepeatedLetters(DataRow row) {
		// TODO Auto-generated method stub
		String name = row.getName();
		
		if (name == null || name.isEmpty()) {return false;}
		
		return name.matches("^.*(.)\\1{2,}.*$");
		
	}

	public static boolean hasRepeatedWords(DataRow row) {
		// TODO Auto-generated method stub
		String name = row.getName();
		
		if (name == null || name.isEmpty()) {return false;}	
		
		String splitWords[] = name.split(" ");
		
		if (splitWords.length == 1) return false;
		
		for (int i = 0; i < splitWords.length; i++) {
			String word = splitWords[i];
			for (int j = i + 1; j < splitWords.length; j++) {
				if (word.equals(splitWords[j])) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	public static int calculateAge(DataRow row) {
		try {
			LocalDate birthDate = LocalDate.parse(row.getDate());
			LocalDate now = LocalDate.now();
			
			if (birthDate.isAfter(now)) {
				return -1;
			}
			
			return Period.between(birthDate, now).getYears();
			
		}catch(DateTimeParseException e) {
			e.printStackTrace();
			return -1;
		}
		
	}

	public static boolean ageTooOld(DataRow row) {
		// TODO Auto-generated method stub
		int age = calculateAge(row);
		return age>110;
	}
	
	public static boolean ageTooYoung(DataRow row) {
		// TODO Auto-generated method stub
		int age = calculateAge(row);
		return age<=1;
	}
}
