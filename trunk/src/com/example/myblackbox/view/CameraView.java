package com.example.myblackbox.view;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.FileUploader;
import com.example.myblackbox.etc.GeoInfo;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;
import com.example.myblackbox.etc.ShakeEventListener;
import com.example.myblackbox.etc.UploadData;
import com.example.myblackbox.etc.gsHttpConnect;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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
	
	
	private int theRestUploadData = 0;
	


	/** Test Timer */
	private UploadDataTimerJob theUpladTimerJob;
	private Timer theTimer;

	/** Shake Sensor */
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	private boolean isShake = false;
	
	/** Test ArrayList */
	private ArrayList<String> TestArrayList = new ArrayList<String>();

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
		theShakeBtn = (Button) findViewById(R.id.shake);
		theNoneBtn = (Button) findViewById(R.id.none);

		theStartRecordBtn.setOnClickListener(theButtonListener);
		theStopRecordBtn.setOnClickListener(theButtonListener);
		theShakeBtn.setOnClickListener(theButtonListener);
		theNoneBtn.setOnClickListener(theButtonListener);

		// Date Format Setting
		theFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.KOREA);

		// Upload Data Setting
		theUploadDataSet = new ArrayList<UploadData>(10);

		// Shake Sensor Setting
		mSensorListener = new ShakeEventListener();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		mSensorListener.setMinForce(ShakeEventListener.MIN_FORCE_3);
		mSensorListener
				.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

					public void onShake() {
						// TODO Auto-generated method stub
						if (GlobalVar.isDebug)
							Log.e(GlobalVar.TAG, "Shake()");
						if(isShake == true) {
							theRestUploadData = 2;
							
							
							
						} else {
							isShake = false;
						}

					}
				});
		
		TestThread test = new TestThread();
		test.start();
	}
	
	private int TestInt = 0;

	OnClickListener theButtonListener = new OnClickListener() {

		public void onClick(View v) {
			// TODO Auto-generated method stub

			Message theMsg;
			switch (v.getId()) {
			case R.id.start_record:
				theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
						GlobalVar.BLUE_REQ_OBD_INFO, 10, 0);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

				startRecord();
				startRecordTimer();

				break;
			case R.id.stop_record:
				theMsg = theGlobalVar.theBlueCommandHandler
						.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
				theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

				stopRecordTimer();

				break;
			case R.id.shake:
				sendUploadData();
				break;
			case R.id.none:
				TestInt++;
				TestArrayList.add("Test "+TestInt);

				break;
			default:
				break;
			}

		}
	};

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

				break;
			}
		}
	};

	private class TestThread extends Thread {
		public TestThread() {
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
			
			
			try {
				while(true) {
					Thread.sleep(1000);
					for(int i = 0; i < TestArrayList.size() ; i++) {
						Log.e(GlobalVar.TAG,"Test : "+TestArrayList.get(i));
					
						
					}
					
				}
				
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	// //////////////////////UPload ?///////////////////////////////////////////

	private void sendUploadData() {
		 String theTestPath = "/sdcard/videoTest.jpg";
//		String theTestPath = "/sdcard/a.jpg";

		String theURL = GlobalVar.WEB_URL + "index.php";
		Log.e(GlobalVar.TAG, "" + GlobalVar.WEB_URL + "index.php");

		
		
		
		
		ArrayList<UploadData> temp = new ArrayList<UploadData>();
		
		temp.add(theUploadDataSet.get(theUploadDataSet.size()-2));
		temp.add(theUploadDataSet.get(theUploadDataSet.size()-1));
		temp.add(theUploadDataSet.get(theUploadDataSet.size()));
		
//		UploadThread theUpload = new UploadThread(url, theData);
//		theUpload
		
		
//		UploadThread theUpload = new UploadThread(theTestPath, theURL);
//		theUpload.start();
		

	}

	private void startRecordTimer() {

		theUpladTimerJob = new UploadDataTimerJob();

		theTimer = new Timer();
		theTimer.scheduleAtFixedRate(theUpladTimerJob, 30 * 1000, 30 * 1000);
	}

	private void stopRecordTimer() {
		theTimer.cancel();
		theTimer = null;

		theUpladTimerJob.cancel();
		theUpladTimerJob = null;

	}
	
	
	
	
	
	

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

	}

	private void stopRecord() {
		lastUploadData().setSaved(true);
		
		printUploadData();
		
		
		if(theRestUploadData > 0) {
			theRestUploadData--;
		} else if (theRestUploadData == 0) {
			sendUploadData();
			
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

	public class UploadDataTimerJob extends TimerTask {
		public void run() {
			stopRecord();
			startRecord();

		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////
	// //////////////////////////////////////////////////////////////////////////////////////////

	private class UploadThread extends Thread {
		private String theURL;

		public UploadThread(String url, ArrayList<UploadData> theData) {
			// TODO Auto-generated constructor stub
			theURL = url;
		}
		
		

		public void run() {
/*
			if (GlobalVar.isDebug) {
				Log.e(GlobalVar.TAG, "Start Upload");
			}
			try {
				HttpClient theClient = new DefaultHttpClient();
				
				HttpPost thePost = new HttpPost(theURL);
				MultipartEntity theEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

				ContentBody theFile = new FileBody(new File(theFilePath));

				theEntity.addPart("File", theFile);

				theEntity.addPart("Name", new StringBody("Test"));
				thePost.setEntity(theEntity);

				HttpResponse theResponse = theClient.execute(thePost);

				HttpEntity theResEntity = theResponse.getEntity();
				
				
				String theResponseString = EntityUtils.toString(theResEntity);

				Log.e(GlobalVar.TAG, "Response : " + theResponseString);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

*/
		}
	}
}
