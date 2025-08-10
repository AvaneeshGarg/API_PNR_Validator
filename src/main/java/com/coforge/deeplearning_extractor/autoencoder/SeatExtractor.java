package com.coforge.deeplearning_extractor.autoencoder;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SeatExtractor {

    public static SeatInfo extractSeatInfo(Element parentElement) {
        NodeList seatNodes = parentElement.getElementsByTagName("SeatNumber");
        if (seatNodes.getLength() > 0) {
            Element seatElement = (Element) seatNodes.item(0);
            String seatNumber = seatElement.getTextContent().trim();
            String cabinClass = seatElement.getAttribute("CabinClass").trim();
            return new SeatInfo(seatNumber, cabinClass);
        }
        return null;
    }

    public static class SeatInfo {
        private final String seatNumber;
        private final String cabinClass;

        public SeatInfo(String seatNumber, String cabinClass) {
            this.seatNumber = seatNumber;
            this.cabinClass = cabinClass;
        }
        public String getSeatNumber() {
            return seatNumber;
        }
        public String getCabinClass() {
            return cabinClass;
        }
    }

    public static String anomalyType(SeatInfo seatInfo, DL4JAutoencoderModel model)  {
        String seat = seatInfo.getSeatNumber().strip();

        if (seat.length() > 3) {
            return "Length of seat number cannot be more than 3";
        }
        if (seat.matches(".*[^a-zA-Z0-9].*")) {
            return "Seat Number contains special symbols";
        }
        if (seat.matches("\\d+") || seat.matches("[a-zA-Z]+")) {
            return "SeatNumber needs to alphanumeric. Cannot contain only numbers or only digits";
        }
        String cabin = seatInfo.getCabinClass().strip();
        if (!cabin.isEmpty() && Character.isDigit(cabin.charAt(0))) {
            return "CabinClass cannot be a digit";
        }
        if (!cabin.isEmpty() && cabin.length()>1){
            return "CabinClass cannot exceed length 1";
        }
        return "none";
    }
    
    
}