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
