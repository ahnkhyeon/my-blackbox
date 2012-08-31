package com.example.myblackbox.etc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import android.util.Log;

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
	
	public void createDataFile(String XmlPath, String VideoPath)
			throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException,
			FileNotFoundException {

		UploadData theData = this;
		
		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();

		Element theMyBlackBox = doc.createElement("MyBlackBox");
		doc.appendChild(theMyBlackBox);

		Element theVideoPath = doc.createElement("VideoPath");
		theVideoPath.setTextContent(VideoPath+"/"+theData.getDate()+".mp4");
		theVideoPath.setTextContent(theData.getVideoPath());
		theMyBlackBox.appendChild(theVideoPath);

		Element theDate = doc.createElement("Date");
		theDate.setTextContent(theData.getDate());
		theMyBlackBox.appendChild(theDate);

		Element theObdInfo = doc.createElement("ObdInfo");
		theMyBlackBox.appendChild(theObdInfo);

		for (OBD_Info theObd : theData.getObdInfoSet()) {
			Element tmpObd = doc.createElement("OBD");

			Element tmpObdDate = doc.createElement("obdDate");
			tmpObdDate.setTextContent(theObd.getObdDate());
			tmpObd.appendChild(tmpObdDate);

			Element tmpObdEngineRPM = doc.createElement("obdEngineRPM");
			tmpObdEngineRPM.setTextContent(theObd.getObdEngineRpm());
			tmpObd.appendChild(tmpObdEngineRPM);

			Element tmpObdEngineTemp = doc.createElement("obdEngineTemp");
			tmpObdEngineTemp.setTextContent(theObd.getObdEngineTemp());
			tmpObd.appendChild(tmpObdEngineTemp);

			Element tmpObdThrottlePos = doc.createElement("obdThrottlePos");
			tmpObdThrottlePos.setTextContent(theObd.getObdTrottlePos());
			tmpObd.appendChild(tmpObdThrottlePos);

			Element tmpObdAirFlow = doc.createElement("obdAirFlow");
			tmpObdAirFlow.setTextContent(theObd.getObdAirFlow());
			tmpObd.appendChild(tmpObdAirFlow);

			Element tmpObdSpeed = doc.createElement("obdSpeed");
			tmpObdSpeed.setTextContent(theObd.getObdSpeed());
			tmpObd.appendChild(tmpObdSpeed);

			theObdInfo.appendChild(tmpObd);
		}

		Element theGeoInfo = doc.createElement("GeoInfo");
		theMyBlackBox.appendChild(theGeoInfo);

		for (GeoInfo theGeo : theData.getGeoInfoSet()) {
			Element tmpGeo = doc.createElement("Geo");

			Element tmpGeoDate = doc.createElement("geoDate");
			tmpGeoDate.setTextContent(theGeo.getDate());
			tmpGeo.appendChild(tmpGeoDate);

			Element tmpGeoLat = doc.createElement("geoLat");
			tmpGeoLat.setTextContent(theGeo.getLatitude() + "");
			tmpGeo.appendChild(tmpGeoLat);

			Element tmpGeoLng = doc.createElement("geoLng");
			tmpGeoLng.setTextContent(theGeo.getLongitude() + "");
			tmpGeo.appendChild(tmpGeoLng);

			theGeoInfo.appendChild(tmpGeo);
		}

		// create Transformer object
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();

		// StringWriter writer = new StringWriter();
		// StreamResult result = new StreamResult(writer);
		// transformer.transform(new DOMSource(doc), result);

		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(new File(
				XmlPath + "/" + theData.getDate() + ".xml")));

		transformer.transform(source, result);

		Log.e(GlobalVar.TAG, "Xml File Saved!");

	}
}
