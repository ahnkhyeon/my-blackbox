package com.example.myblackbox.etc;

public class OBD_Info {
	private String obdEngineRPM;
	private String obdEngineTemp;
	private String obdMassAirFlow;
	private String obdThrottlePos;
	private String obdSpeed;
	private String obdDate;
	
	public OBD_Info() {
		// TODO Auto-generated constructor stub
	}
	
	
	public void setObdEngineRpm(String theEngineRpm) {
		obdEngineRPM = theEngineRpm;
	}
	public String getObdEngineRpm() {
		return obdEngineRPM;
	}
	
	public void setObdEngineTemp(String theEngineTemp) {
		obdEngineTemp = theEngineTemp;
	}
	public String getObdEngineTemp() {
		return obdEngineTemp;
	}
	
	public void setObdAirFlow(String theAirFlow) {
		obdMassAirFlow = theAirFlow;
	}
	public String getObdAirFlow() {
		return obdMassAirFlow;
	}
	
	public void setObdThrottlePos(String theThrottlePos) {
		obdThrottlePos = theThrottlePos;
	}
	public String getObdTrottlePos() {
		return obdThrottlePos;
	}
	
	public void setObdSpeed(String theSpeed) {
		obdSpeed = theSpeed;
	}
	public String getObdSpeed() {
		return obdSpeed;
	}
	
	public void setObdDate(String theDate) {
		obdDate = theDate;
	}
	public String getObdDate() {
		return obdDate;
	}
	

}
