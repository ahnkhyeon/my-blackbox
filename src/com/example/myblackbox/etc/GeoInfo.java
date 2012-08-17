package com.example.myblackbox.etc;

public class GeoInfo {
	private double theLatitude;
	private double theLongitude;
	
	public GeoInfo(double latitude, double longitude) {
		// TODO Auto-generated constructor stub
		theLatitude = latitude;
		theLongitude = longitude;
	}
	
	public double getLatitude() {
		return theLatitude;
	}
	public double getLongitude() {
		return theLongitude;
	}
	}
