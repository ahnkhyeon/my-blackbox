package com.example.myblackbox.view;

import java.io.File;
import java.io.IOException;
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
import com.example.myblackbox.etc.gsHttpConnect;

import android.app.Activity;
import android.content.Context;
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
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

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

	/** Test Timer */
	private UploadDataTimerJob theUpladTimerJob;
	private Timer theTimer;

	/** Shake Sensor */
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;

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

					}
				});
	}

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

				break;
			case R.id.none:
				sendUploadData();
				break;
			default:
				break;
			}

		}
	};
	public void upload(String theUrl , String theFile) throws Exception {
        //Url of the server
//        String url = "";
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(theUrl);
        MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
        //Path of the file to be uploaded
        String filepath = "";
        File file = new File(theFile);
        ContentBody cbFile = new FileBody(file, "image/jpeg");         

        //Add the data to the multipart entity
        mpEntity.addPart("image", cbFile);
        mpEntity.addPart("name", new StringBody("Test", Charset.forName("UTF-8")));
        mpEntity.addPart("data", new StringBody("This is test report", Charset.forName("UTF-8")));
        post.setEntity(mpEntity);
        //Execute the post request
        HttpResponse response1 = client.execute(post);
        //Get the response from the server
        HttpEntity resEntity = response1.getEntity();
        String Response=EntityUtils.toString(resEntity);
        Log.d("Response:", Response);
        /*
        //Generate the array from the response
        JSONArray jsonarray = new JSONArray("["+Response+"]");
        JSONObject jsonobject = jsonarray.getJSONObject(0);
        //Get the result variables from response 
        String result = (jsonobject.getString("result"));
        String msg = (jsonobject.getString("msg"));
        //Close the connection
        client.getConnectionManager().shutdown();
        */
    }
	private void sendUploadData() {
		String theTestPath = "/sdcard/videoTest.mp4";
		
		Log.e(GlobalVar.TAG,""+GlobalVar.WEB_URL + "index.php");
		
		
		try {
			upload(GlobalVar.WEB_URL + "index.php", theTestPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

//		try {
//			HttpClient theHttpClient = new DefaultHttpClient();
//			HttpPost theHttpPost = new HttpPost(GlobalVar.WEB_URL + "index.php");
//
//			MultipartEntity theEntity = new MultipartEntity();
//
//			theEntity.addPart("Name", new StringBody("kiSeong"));
//			theEntity.addPart("File", new FileBody(new File(theTestPath)));
//
//			theHttpPost.setEntity(theEntity);
//			HttpResponse theResponse = theHttpClient.execute(theHttpPost);
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ClientProtocolException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// FileUploader theUploader = new FileUploader(theTestPath,
		// GlobalVar.WEB_URL + "index.php");
		// String theResult = theUploader.uploadFile();
		//
		// Log.e(GlobalVar.TAG, "Result : " + theResult);

		//
		// gsHttpConnect theUploader = new gsHttpConnect();
		//
		// Map<String, Object> params = new HashMap<String, Object>();
		//
		// params.put("user_id", "theId");
		// params.put("user_pw", "thePass");
		//
		// Map<String, Object> files = new HashMap<String, Object>();
		//
		// files.put("file1", theTestPath);
		//
		// try {
		// String theResult = theUploader.uploadAndRequest(new
		// URL(GlobalVar.WEB_URL+"index.php"), params, files);
		// Log.e(GlobalVar.TAG,"Result : "+theResult);
		// } catch (MalformedURLException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

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

	/** Custom Hardware Button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Back");

			Message theMsg = theGlobalVar.theBlueCommandHandler
					.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

			if (theGlobalVar == null) {
				theGlobalVar = (GlobalVar) getApplicationContext();
			}

			theGlobalVar.theCameraHandler = null;

			finish();

			return false;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Valume Up");
			return false;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (GlobalVar.isDebug)
				Log.e(GlobalVar.TAG, "KeyCode Balume Down");
			return false;
		}

		return true;
	}

	public class UploadDataTimerJob extends TimerTask {
		public void run() {
			stopRecord();
			startRecord();

		}
	}

	/** UpladData Class */
	private class UploadData {
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

		public void addObdInfo(OBD_Info obdInfo) {
			theObdInfoSet.add(obdInfo);
		}

		public void addGeoInfo(GeoInfo geoInfo) {
			theGeoInfoSet.add(geoInfo);
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

}
