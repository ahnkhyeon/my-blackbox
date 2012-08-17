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

//	public UploadData(UploadData theUploadData) {
//
//		theDate = theUploadData.getDate();
//		theVideoPath = theUploadData.getVideoPath();
//
//		theObdInfoSet = new ArrayList<OBD_Info>();
//		theGeoInfoSet = new ArrayList<GeoInfo>();
//
//		ArrayList<OBD_Info> tempObdSet = new ArrayList<OBD_Info>();
//		ArrayList<GeoInfo> tempGeoSet = new ArrayList<GeoInfo>();
//		for (int i = 0; i < theUploadData.getObdInfoSet().size(); i++) {
//			theObdInfoSet.add(theUploadData.get)
//
//		}
//
//		for (int i = 0; i < theUploadData.getGeoInfoSet().size(); i++) {
//
//		}
//
//	}

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
