package com.example.myblackbox.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;

import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.GeoInfo;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;
import com.example.myblackbox.etc.ShakeEventListener;
import com.example.myblackbox.etc.UploadData;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

public class CameraView extends Activity {

	/** Global Variable */
	private GlobalVar theGlobalVar;

	/** Test UI */
	private Button theStartRecordBtn;
	private Button theStopRecordBtn;
	private Button theShakeBtn;
	private Button theNoneBtn;

	/** Date Format */
	private SimpleDateFormat theFormat;

	/** Upload Data */
	private ArrayList<UploadData> theUploadDataSet;
	private final static int MAX_UPLOAD_DATA = 10;

	private int theRestUploadData = -1;

	/** Test Timer */
	private UploadDataTimerJob theUpladTimerJob;
	private Timer theTimer;
	private final static int UPLOAD_INTAVAL = 30;

	/** Shake Sensor */
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	private boolean isShake = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		setContentView(R.layout.camera_view);

		if (theGlobalVar == null) {
			theGlobalVar = (GlobalVar) getApplicationContext();
		}
		theGlobalVar.theCameraHandler = mHandler;

		
		
		theStartRecordBtn = (Button) findViewById(R.id.start_record);
		theStopRecordBtn = (Button) findViewById(R.id.stop_record);
		theStopRecordBtn.setEnabled(false);
		theShakeBtn = (Button) findViewById(R.id.shake);
		theNoneBtn = (Button) findViewById(R.id.none);

		theStartRecordBtn.setOnClickListener(theButtonListener);
		theStopRecordBtn.setOnClickListener(theButtonListener);
		theShakeBtn.setOnClickListener(theButtonListener);
		theNoneBtn.setOnClickListener(theButtonListener);

		// Date Format Setting
		theFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);

		// Upload Data Setting
		theUploadDataSet = new ArrayList<UploadData>(10);

		// Shake Sensor Setting
		mSensorListener = new ShakeEventListener();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		// shake 센서 감도
		mSensorListener.setMinForce(ShakeEventListener.MIN_FORCE_3);
		mSensorListener
				.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

					public void onShake() {
						// TODO Auto-generated method stub
//						if (theStartRecordBtn.isEnabled() == true) {
//							return;
//						}

						if (getShakeState() == false) {
							

							setShakeState(true);
							theRestUploadData = 2;
							
							if(theUploadDataSet.size() > 1) {
								addSendPool(theUploadDataSet.get(theUploadDataSet.size()-2));
							} else {
								Log.e(GlobalVar.TAG,"아직 업로드 할 내용이 없음");
							}
						}
					}
				});
	}

	private void setShakeState(boolean shake) {
		isShake = shake;

		if (GlobalVar.isDebug) {
			if (shake) {
				Log.e(GlobalVar.TAG, "Shake() ON");
			} else {
				Log.e(GlobalVar.TAG, "Shake() OFF");
			}
		}

	}

	private synchronized boolean getShakeState() {
		return isShake;
	}

	OnClickListener theButtonListener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub

			Message theMsg;
			switch (v.getId()) {
			case R.id.start_record:
				theStartRecordBtn.setEnabled(false);
				theStopRecordBtn.setEnabled(true);
				theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
						GlobalVar.BLUE_REQ_OBD_INFO, 10, 0);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

				startRecord();
				startRecordTimer();

				break;
			case R.id.stop_record:
				theStartRecordBtn.setEnabled(true);
				theStopRecordBtn.setEnabled(false);

				theMsg = theGlobalVar.theBlueCommandHandler
						.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

				stopRecordTimer();

				break;
			case R.id.shake:

				UploadData theTempData = theUploadDataSet.get(0);

				HttpClient theClient = new DefaultHttpClient();

				HttpPost thePost = new HttpPost(GlobalVar.WEB_URL
						+ "test.php");
				MultipartEntity theEntity = new MultipartEntity();

				 ContentBody theFile = new FileBody(new File(
				 theTempData.getVideoPath()));
				 theEntity.addPart("video", theFile);

				 ;
				try {
					theEntity.addPart("user", new StringBody(theGlobalVar.getSharedPref(GlobalVar.SHARED_LOGIN_ID)));
					theEntity.addPart("identity", new StringBody(theGlobalVar.getSharedPref(GlobalVar.SHARED_LOGIN_IDENTITY)));
					
//					theEntity.addPart("ObdInfo", new StringBody(
//							CreateXmlOBD(theTempData.getObdInfoSet())));
//					theEntity.addPart("GeoInfo", new StringBody(
//							CreateXmlGeo(theTempData.getGeoInfoSet())));
//				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				} catch (TransformerFactoryConfigurationError e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
//				} catch (TransformerException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				thePost.setEntity(theEntity);

				try {
					HttpResponse theResponse = theClient.execute(thePost);
					HttpEntity theResEntity = theResponse.getEntity();

					String theResponseString = EntityUtils
							.toString(theResEntity);

					Log.e(GlobalVar.TAG, "Response : " + theResponseString);

				} catch (ClientProtocolException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				break;
			case R.id.none:

//				ArrayList<GeoInfo> tempGeoInfoSet = new ArrayList<GeoInfo>();
//
//				GeoInfo tempGeo = new GeoInfo(10.0, 20.0, "20120203");
//
//				tempGeoInfoSet.add(tempGeo);
//
//				try {
//					String Xml = CreateXmlGeo(tempGeoInfoSet);
//					Log.e(GlobalVar.TAG, "Xml : " + Xml);
//				} catch (TransformerFactoryConfigurationError e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ParserConfigurationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (TransformerException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}

				// try {
				// Document doc = DocumentBuilderFactory.newInstance()
				// .newDocumentBuilder().newDocument();
				// // create root: <record>
				// Element root = doc.createElement("root");
				// doc.appendChild(root);
				//
				// // create: <study>
				// Element tagStudy = doc.createElement("Study");
				// root.appendChild(tagStudy);
				// tagStudy.setTextContent("Text");
				//
				//
				//
				// // create Transformer object
				// Transformer transformer = TransformerFactory.newInstance()
				// .newTransformer();
				// StringWriter writer = new StringWriter();
				// StreamResult result = new StreamResult(writer);
				// transformer.transform(new DOMSource(doc), result);
				//
				// // return XML string
				// Log.e(GlobalVar.TAG,"XML : "+writer.toString());
				//
				// } catch (ParserConfigurationException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (TransformerConfigurationException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (TransformerFactoryConfigurationError e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// } catch (TransformerException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }

				break;
			default:
				break;
			}

		}
	};
	
	private void addSendPool(UploadData theData) {
		Message theMsg = theGlobalVar.theUploadHandler.obtainMessage(GlobalVar.ADD_UPLOAD_DATA, theData);
		theGlobalVar.theUploadHandler.sendMessage(theMsg);							
		
	}

	
	/** OBD 정보 추가 핸들러 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GlobalVar.OBD_INFO_FROM_CAMERA:

				// 데이터 들어왔을때

				OBD_Info theInputData = (OBD_Info) msg.obj;

				// Log.e(GlobalVar.TAG, "Camera : " +
				// theInputData.getObdDate());

				UploadData theUploadData = lastUploadData();

				theUploadData.addObdInfo(theInputData);
				
				GeoInfo geoInfo = new GeoInfo(Double.parseDouble(theInputData.getObdAirFlow()), Double.parseDouble(theInputData.getObdEngineTemp()), theInputData.getObdDate());
				
				theUploadData.addGeoInfo(geoInfo);

				break;
			}
		}
	};


	
	/** 영상 저장 테스트 */

	private void startRecordTimer() {

		theUpladTimerJob = new UploadDataTimerJob();

		theTimer = new Timer();
		theTimer.scheduleAtFixedRate(theUpladTimerJob, UPLOAD_INTAVAL * 1000,
				UPLOAD_INTAVAL * 1000);
	}

	private void stopRecordTimer() {
		theTimer.cancel();
		theTimer = null;

		theUpladTimerJob.cancel();
		theUpladTimerJob = null;
	}

	public class UploadDataTimerJob extends TimerTask {
		public void run() {
			stopRecord();
			startRecord();

		}
	}

	/** 영상 녹화 정지 */
	private void startRecord() {
		if (GlobalVar.isDebug) {
			Log.e(GlobalVar.TAG, "StartRecord");
		}

		String theDate = theFormat.format(new Date());

		if (theUploadDataSet.size() >= MAX_UPLOAD_DATA) {
			theUploadDataSet.remove(0);
		}

		UploadData uploadData = new UploadData(theDate);

		theUploadDataSet.add(uploadData);

//		String theTestPath = "/sdcard/videoTest.mp4";
		String theTestPath = "/sdcard/a.jpg";

		FileCopy theCopy = new FileCopy(theTestPath, GlobalVar.VIDEO_PATH + "/"
				+ theDate + ".mp4");
		theCopy.start();
	}

	private void stopRecord() {
		lastUploadData().setSaved(true);

		printUploadData();

		if (theRestUploadData > 0) {
			theRestUploadData--;
			addSendPool(lastUploadData());
//			theTempDataSet.add(lastUploadData());
		} else if (theRestUploadData == 0) {
			setShakeState(false);
			theRestUploadData = -1;
		}

	}

	private UploadData lastUploadData() {
		if (!theUploadDataSet.isEmpty()) {
			return theUploadDataSet.get(theUploadDataSet.size() - 1);
		} else {
			return null;
		}
	}

	private void printUploadData() {

		for (int i = 0; i < theUploadDataSet.size(); i++) {
			// Log.e(GlobalVar.TAG, "Data "+i+" : " +
			// theUploadDataSet.get(i).theDate + " / "+theUploadDataSet.get(i));
			Log.e(GlobalVar.TAG, "Data " + i + " : "
					+ theUploadDataSet.get(i).printObdData());
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/** File Copy */

	public void fileCopy(String srcPath, String dstPath) throws IOException {

		FileInputStream inputStream = new FileInputStream(new File(srcPath));
		FileOutputStream outputStream = new FileOutputStream(new File(dstPath));

		FileChannel fcin = inputStream.getChannel();
		FileChannel fcout = outputStream.getChannel();

		long size = fcin.size();

		fcin.transferTo(0, size, fcout);
		fcout.close();
		fcin.close();
		outputStream.close();
		inputStream.close();
	}

	
	
	private class FileCopy extends Thread {
		File theSrc;
		File theDst;

		public FileCopy(String src, String dst) {
			// TODO Auto-generated constructor stub

			theSrc = new File(src);
			theDst = new File(dst);
			// Log.e(GlobalVar.TAG, "Dest : " + dst);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();

			try {
				FileInputStream inputStream = new FileInputStream(theSrc);
				FileOutputStream outputStream = new FileOutputStream(theDst);

				FileChannel fcin = inputStream.getChannel();
				FileChannel fcout = outputStream.getChannel();

				long size = fcin.size();

				fcin.transferTo(0, size, fcout);
				fcout.close();
				fcin.close();
				outputStream.close();
				inputStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
