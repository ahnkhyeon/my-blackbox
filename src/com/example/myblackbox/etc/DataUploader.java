package com.example.myblackbox.etc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DataUploader extends Thread {

	private final static int UPLOAD_TIME_OUT = 10;

	private String theUser;
	private String theIdentity;

	private ArrayList<UploadData> theUploadPool;
	private String theUploadURL;

	private Object mPauseLock;
	private boolean mPaused;
	private boolean mFinished;

	private Handler mHandler;
	
	public DataUploader(ArrayList<UploadData> thePool, String url, String user,
			String identity, Handler handler) {
		// TODO Auto-generated constructor stub

		theUser = user;
		theIdentity = identity;

		theUploadPool = thePool;
		theUploadURL = url;

		mPauseLock = new Object();
		mPaused = false;
		mFinished = false;

		mHandler = handler;
		

	}

	public void run() {
		// Log.e(GlobalVar.TAG, "UploaderThread Start");
		Thread.currentThread().setName("UploaderThread");

		while (!mFinished) {
			// Do something you need!!

			if (theUploadPool.isEmpty()) {
				onPause();
			} else {
				if (Uploader(theUploadPool.get(0))) {
					Log.e(GlobalVar.TAG,
							"Upload Itmes : " + theUploadPool.size());
					theUploadPool.remove(0);
				}
			}

			synchronized (mPauseLock) {
				while (mPaused) {
					try {
						mPauseLock.wait();
					} catch (InterruptedException e) {
					}
				}
			}
		}
	}
	
	
	

	/**
	 * Pause this thread
	 */
	public void onPause() {
		synchronized (mPauseLock) {
			mPaused = true;
		}
	}

	/**
	 * Resume this thread
	 */
	public void onResume() {
		synchronized (mPauseLock) {
			mPaused = false;
			mPauseLock.notifyAll();
		}
	}
	
	public void setUserInfo(String id,String identity) {
		theUser = id;
		theIdentity = identity;
	}
	

	private boolean Uploader(UploadData theData) {
		boolean isSuccess = true;
		String errString = "";

		try {
			Log.e(GlobalVar.TAG, "Start Upload");
		
			HttpClient theClient = new DefaultHttpClient();

			HttpPost thePost = new HttpPost(theUploadURL);

			MultipartEntity theEntity = new MultipartEntity();

			// Time Out setting
			theClient.getParams().setParameter("http.protocol.expect-continue",
					false);
			theClient.getParams().setParameter("http.connection.timeout",
					UPLOAD_TIME_OUT * 1000);
			theClient.getParams().setParameter("http.socket.timeout",
					UPLOAD_TIME_OUT * 1000);

			// HttpParams theHttpParams = theClient.getParams();
			// theHttpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
			// HttpVersion.HTTP_1_1);
			// HttpConnectionParams.setConnectionTimeout(theHttpParams,
			// UPLOAD_TIME_OUT * 1000);
			// HttpConnectionParams.setSoTimeout(theHttpParams,
			// UPLOAD_TIME_OUT * 1000);

			File theViedoFile = new File(theData.getVideoPath());

			ContentBody theFile = new FileBody(theViedoFile);

			theEntity.addPart("video", theFile);

			theEntity.addPart("ObdInfo",
					new StringBody(CreateXmlOBD(theData.getObdInfoSet())));
			theEntity.addPart("GeoInfo",
					new StringBody(CreateXmlGeo(theData.getGeoInfoSet())));

			theEntity.addPart("user", new StringBody(theUser));
			theEntity.addPart("identity", new StringBody(theIdentity));

			thePost.setEntity(theEntity);

			HttpResponse theResponse = theClient.execute(thePost);

			HttpEntity theResEntity = theResponse.getEntity();

			String theResponseString = EntityUtils.toString(theResEntity);
			
			
			errString = chkError(theResponseString);
			Log.e(GlobalVar.TAG,"Error : "+errString);

			Log.e(GlobalVar.TAG, "Uploaded : " + theResponseString);

			// TODO 에러 발생시
			// isSuccess = false;

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 1");
			isSuccess = false;

		} catch (TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 2");
			isSuccess = false;

		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 3");
			isSuccess = false;

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 4");
			isSuccess = false;

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 5");
			isSuccess = false;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// Log.e(GlobalVar.TAG,"Upload Exception 6"+e);
			isSuccess = false;
		}
		
		if(errString.length() != 0) {
			Message msg = mHandler
					.obtainMessage(BluetoothService.MESSAGE_TOAST);
			Bundle bundle = new Bundle();
			bundle.putString(BluetoothService.TOAST, errString);
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		} else if (isSuccess == false) {

			Message msg = mHandler
					.obtainMessage(BluetoothService.MESSAGE_TOAST);
			Bundle bundle = new Bundle();
			bundle.putString(BluetoothService.TOAST, "업로드에 오류가 발생하였습니다.");
			msg.setData(bundle);
			mHandler.sendMessage(msg);
		}
		
		errString="";

		return true;
	}

	// Geo 정보를 Xml로 만들어주는 매서드
	private String CreateXmlGeo(ArrayList<GeoInfo> geoInfo)
			throws TransformerFactoryConfigurationError,
			ParserConfigurationException, TransformerException {

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();

		Element theGeoInfo = doc.createElement("GeoInfo");
		doc.appendChild(theGeoInfo);

		if (geoInfo.isEmpty()) {
			GeoInfo theEmptyGeo = new GeoInfo(-1, -1, "-1");

			geoInfo.add(theEmptyGeo);
		}

		for (GeoInfo geo_Info : geoInfo) {

			// create:<Geo>
			Element theGeo = doc.createElement("Geo");
			theGeoInfo.appendChild(theGeo);

			// create:<date>
			Element theDate = doc.createElement("date");
			theDate.setTextContent(geo_Info.getDate());
			theGeo.appendChild(theDate);

			// create:<Latitude>
			Element theLatitude = doc.createElement("Latitude");
			theLatitude.setTextContent(Double.toString(geo_Info.getLatitude()));
			theGeo.appendChild(theLatitude);

			// create:<Longitude>
			Element theLongitude = doc.createElement("Longitude");
			theLongitude
					.setTextContent(Double.toString(geo_Info.getLongitude()));
			theGeo.appendChild(theLongitude);

		}

		// create Transformer object
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(new DOMSource(doc), result);

		return writer.toString();

	}

	// OBD 정보를 Xml로 만들어주는 매서드
	private String CreateXmlOBD(ArrayList<OBD_Info> obdInfo)
			throws ParserConfigurationException,
			TransformerFactoryConfigurationError, TransformerException {

		Document doc = DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument();

		Element theObdInfo = doc.createElement("OBD_Info");
		doc.appendChild(theObdInfo);

		if (obdInfo.isEmpty()) {
			OBD_Info theEmptyOBD = new OBD_Info();
			theEmptyOBD.setObdAirFlow("-1");
			theEmptyOBD.setObdDate("-1");
			theEmptyOBD.setObdEngineRpm("-1");
			theEmptyOBD.setObdEngineTemp("-1");
			theEmptyOBD.setObdSpeed("-1");
			theEmptyOBD.setObdThrottlePos("-1");

			obdInfo.add(theEmptyOBD);
		}

		for (OBD_Info obd_Info : obdInfo) {

			// create:<OBD>
			Element theObd = doc.createElement("OBD");
			theObdInfo.appendChild(theObd);

			// create:<date>
			Element theDate = doc.createElement("date");
			theDate.setTextContent(obd_Info.getObdDate());
			theObd.appendChild(theDate);

			// create:<EngineRPM>
			Element theEngineRPM = doc.createElement("EngineRPM");
			theEngineRPM.setTextContent(obd_Info.getObdEngineRpm());
			theObd.appendChild(theEngineRPM);

			// create:<EngineTemp>
			Element theEngineTemp = doc.createElement("EngineTemp");
			theEngineTemp.setTextContent(obd_Info.getObdEngineTemp());
			theObd.appendChild(theEngineTemp);

			// create:<ThrottlePos>
			Element theThrottlePos = doc.createElement("ThrottlePos");
			theThrottlePos.setTextContent(obd_Info.getObdTrottlePos());
			theObd.appendChild(theThrottlePos);

			// create:<AirFlow>
			Element theAirFlow = doc.createElement("AirFlow");
			theAirFlow.setTextContent(obd_Info.getObdAirFlow());
			theObd.appendChild(theAirFlow);

			// create:<Speed>
			Element theSpeed = doc.createElement("Speed");
			theSpeed.setTextContent(obd_Info.getObdSpeed());
			theObd.appendChild(theSpeed);
		}

		// create Transformer object
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		StringWriter writer = new StringWriter();
		StreamResult result = new StreamResult(writer);
		transformer.transform(new DOMSource(doc), result);

		return writer.toString();

	}

	private String chkError(String theXml) {

		try {
			XmlPullParserFactory parserCreator = XmlPullParserFactory
					.newInstance();
			XmlPullParser parser = parserCreator.newPullParser();

			InputStream inputStream = new ByteArrayInputStream(
					theXml.getBytes("UTF-8"));

			parser.setInput(inputStream, null);

			int parserEvent = parser.getEventType();
			boolean isError = false;
			while (parserEvent != XmlPullParser.END_DOCUMENT) {
				switch (parserEvent) {

				case XmlPullParser.TEXT:
					if(isError) {
						return parser.getText();
					}

					break;
				case XmlPullParser.END_TAG:

					break;
				case XmlPullParser.START_TAG:
					if(parser.getName().equals("error")) {
						
						isError = true;
					}
					break;
				}
				parserEvent = parser.next();
			}

		} catch (XmlPullParserException e) {

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";

	}


}

/*
 * private class UploadThread extends Thread { private String theURL;
 * 
 * public UploadThread(String url) { // TODO Auto-generated constructor stub
 * theURL = url; }
 * 
 * public void run() {
 * 
 * UploadData theTempData; while (!theTempDataSet.isEmpty() || getShakeState()
 * == true) { //Log.e(GlobalVar.TAG, "UploadThread"); if
 * (theTempDataSet.isEmpty()) { try { Thread.sleep(2000); } catch
 * (InterruptedException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } continue; }
 * 
 * theTempData = theTempDataSet.get(0);
 * 
 * try { HttpClient theClient = new DefaultHttpClient();
 * 
 * HttpPost thePost = new HttpPost(theURL); MultipartEntity theEntity = new
 * MultipartEntity();
 * 
 * ContentBody theFile = new FileBody(new File( theTempData.getVideoPath()));
 * theEntity.addPart("video", theFile);
 * 
 * try { theEntity.addPart("ObdInfo", new StringBody(
 * CreateXmlOBD(theTempData.getObdInfoSet()))); theEntity.addPart("GeoInfo", new
 * StringBody( CreateXmlGeo(theTempData.getGeoInfoSet()))); } catch
 * (ParserConfigurationException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } catch (TransformerFactoryConfigurationError e) { //
 * TODO Auto-generated catch block e.printStackTrace(); } catch
 * (TransformerException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); }
 * 
 * thePost.setEntity(theEntity);
 * 
 * HttpResponse theResponse = theClient.execute(thePost);
 * 
 * HttpEntity theResEntity = theResponse.getEntity();
 * 
 * String theResponseString = EntityUtils .toString(theResEntity);
 * 
 * //Log.e(GlobalVar.TAG, "Response : " + theResponseString); } catch
 * (UnsupportedEncodingException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } catch (ClientProtocolException e) { // TODO
 * Auto-generated catch block e.printStackTrace(); } catch (IOException e) { //
 * TODO Auto-generated catch block e.printStackTrace(); }
 * 
 * theTempDataSet.remove(0); } } }
 */