package com.example.myblackbox.etc;

public class GeoInfo {
	private double theLatitude;
	private double theLongitude;
	private String theDate; 

	public GeoInfo(double latitude, double longitude, String date) {
		// TODO Auto-generated constructor stub
		theLatitude = latitude;
		theLongitude = longitude;
		theDate = date;
	}
 	
	public double getTheLatitude() {
		return theLatitude;
	}

	public void setTheLatitude(double theLatitude) {
		this.theLatitude = theLatitude;
	}

	public double getTheLongitude() {
		return theLongitude;
	}

	public void setTheLongitude(double theLongitude) {
		this.theLongitude = theLongitude;
	}

	public String getTheDate() {
		return theDate;
	}

	public void setTheDate(String theDate) {
		this.theDate = theDate;
	}

	public GeoInfo() {
		
	}

	public double getLatitude() {
		return theLatitude;
	}

	public double getLongitude() {
		return theLongitude;
	}
	
	public String getDate() {
		return theDate;
	}
}
