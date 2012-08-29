package com.example.myblackbox.etc;

import java.util.ArrayList;

/** UpladData Class */
public class UploadData {
	private String theVideoPath;
	private String theDate;
	private ArrayList<OBD_Info> theObdInfoSet;
	private ArrayList<GeoInfo> theGeoInfoSet;

	private boolean isSaved = false;

	public UploadData(String date) {
		// TODO Auto-generated constructor stub
		theDate = date;
		theVideoPath = GlobalVar.VIDEO_PATH + "/" + date + ".mp4";

		theObdInfoSet = new ArrayList<OBD_Info>();
		theGeoInfoSet = new ArrayList<GeoInfo>();
	}

	public UploadData(UploadData theData) {
		theDate = theData.getDate();
		theVideoPath = theData.getVideoPath();

		theObdInfoSet = new ArrayList<OBD_Info>();
		theGeoInfoSet = new ArrayList<GeoInfo>();

		ArrayList<OBD_Info> tempObdSet = theData.getObdInfoSet();
		ArrayList<GeoInfo> tempGeoSet = theData.getGeoInfoSet();

		for (int i = 0; i < tempObdSet.size(); i++) {
			OBD_Info tempObdInfo = new OBD_Info();

			tempObdInfo.setObdAirFlow(tempObdSet.get(i).getObdAirFlow());
			tempObdInfo.setObdDate(tempObdSet.get(i).getObdDate());
			tempObdInfo.setObdEngineRpm(tempObdSet.get(i).getObdEngineRpm());
			tempObdInfo.setObdEngineTemp(tempObdSet.get(i).getObdEngineTemp());
			tempObdInfo.setObdSpeed(tempObdSet.get(i).getObdSpeed());
			tempObdInfo.setObdThrottlePos(tempObdSet.get(i).getObdTrottlePos());

			theObdInfoSet.add(tempObdInfo);
		}

		for (int i = 0; i < tempGeoSet.size(); i++) {
			GeoInfo tempGeoInfo = new GeoInfo(tempGeoSet.get(i).getLatitude(),
					tempGeoSet.get(i).getLongitude(),
					tempGeoSet.get(i)	.getDate());
			
			theGeoInfoSet.add(tempGeoInfo);
		}
	}

	public void addObdInfo(OBD_Info obdInfo) {
		theObdInfoSet.add(obdInfo);
	}

	public void addGeoInfo(GeoInfo geoInfo) {
		theGeoInfoSet.add(geoInfo);
	}

	public String getDate() {
		return theDate;
	}

	public String getVideoPath() {
		return theVideoPath;
	}

	public ArrayList<OBD_Info> getObdInfoSet() {
		return theObdInfoSet;
	}

	public ArrayList<GeoInfo> getGeoInfoSet() {
		return theGeoInfoSet;
	}

	public void setSaved(boolean flag) {
		isSaved = flag;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public String printObdData() {
		String thePrint;
		thePrint = "" + theObdInfoSet.size();
		for (int i = 0; i < theObdInfoSet.size(); i++) {
			thePrint += "/" + theObdInfoSet.get(i).getObdDate();
		}
		return thePrint;
	}
}
