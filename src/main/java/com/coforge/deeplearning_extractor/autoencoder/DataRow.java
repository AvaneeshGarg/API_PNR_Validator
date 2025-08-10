package com.coforge.deeplearning_extractor.autoencoder;

public class DataRow {
	private String name;
	private String date;
	private String iata;
	private String seatNumber;
	private String cabinClass;
	
	// Default constructor required for JSON deserialization
	public DataRow() {
	}
	
	public DataRow(String name, String date, String iata, String seatNumber, String cabinClass) {
		super();
		this.name = name;
		this.date = date;
		this.iata = iata;
		this.seatNumber = seatNumber;
		this.cabinClass = cabinClass;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}

	public String getIata() {
		return iata;
	}

	public void setIata(String iata) {
		this.iata = iata;
	}

	public String getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(String seatNumber) {
		this.seatNumber = seatNumber;
	}

	public String getCabinClass() {
		return cabinClass;
	}

	public void setCabinClass(String cabinClass) {
		this.cabinClass = cabinClass;
	}
	
	
	

	
	
	

}
