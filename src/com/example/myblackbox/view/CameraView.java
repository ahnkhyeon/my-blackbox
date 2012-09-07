package com.example.myblackbox.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import android.R.integer;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.myblackbox.R;
import com.example.myblackbox.etc.BluetoothService;
import com.example.myblackbox.etc.GeoInfo;
import com.example.myblackbox.etc.GlobalVar;
import com.example.myblackbox.etc.OBD_Info;
import com.example.myblackbox.etc.ShakeEventListener;
import com.example.myblackbox.etc.UploadData;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class CameraView extends MapActivity implements SurfaceHolder.Callback {

	public static final String TAG = "Camera";
	String MediaPath = GlobalVar.VIDEO_PATH + "/";
	String LOG_TAG = "mapcheck";
	private static String EXTERNAL_STORAGE_PATH = "";
	private static String RECORDED_FILE = "video_recorded";
	private static int fileIndex = 0;
	private static String filename = "";
	private ArrayList<File> Flist;
	MediaPlayer player;
	MediaRecorder recorder;
	boolean isStorage = true;
	boolean isMap = true;
	boolean PreviewOn = false;
	private Camera mCamera = null;

	public int MapTime = 7000;
	SurfaceHolder holder;
	StorageThread ST = new StorageThread();
	MapThread MT = new MapThread();

	private MapView mapView; // 맵뷰 객체
	private List<Overlay> listOfOverlays; // 맵에 표시된 오버레이(레이어)들을 가지고 있는 리스트
	private String bestProvider; // 현재 위치값을 가져오기위한 프로바이더. (network, gps)

	private LocationManager locM; // 위치 매니저
	private LocationListener locL; // 위치 리스너
	private Location currentLocation; // 현재 위치
	private MapController mapController; // 맵을 줌시키거나, 이동시키는데 사용될 컨트롤러

	private LocationItemizedOverlay overlayHere; // 현재위치 마커가 표시되어질 오버레이

	private Button recordBtn;
	private Button recordStopBtn;
	private Button mapHiddenBtn;

	private boolean Mapon = true;
	// ////////////////////////////////////////////////////////////////////////

	private int CameraQuailty;
	private long StorageSize;
	private int RecordTime;

	/** Global Variable */
	private GlobalVar theGlobalVar;

	/** Upload Data */
	private ArrayList<UploadData> theUploadDataSet;
	private final static int MAX_UPLOAD_DATA = 10;

	private int theRestUploadData = -1;
	private static final int NUMBER_UPLOAD_ITEM = 3;

	/** Shake Sensor */
	private SensorManager mSensorManager;
	private ShakeEventListener mSensorListener;
	private boolean isShake = false;

	// //////////////////////////////////////////////////////////////////////

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_view);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		/** GlobalVariable Set */
		theGlobalVar = (GlobalVar) getApplicationContext();

		theGlobalVar.theCameraHandler = mHandler;

		CheckBluetooth();

		/** Camera Set Info */

		CameraQuailty = Integer.parseInt(theGlobalVar
				.getSharedPref(GlobalVar.SHARED_CAMERA_QUAILTY));
		StorageSize = (long) Integer.parseInt(theGlobalVar
				.getSharedPref(GlobalVar.SHARED_CAMERA_STORAGE_SIZE))
				* (long) 1024 * (long) 1024 * (long) 1024;
		// Log.e(GlobalVar.TAG, "StorageSiae : " + StorageSize);

		RecordTime = Integer.parseInt(theGlobalVar
				.getSharedPref(GlobalVar.SHARED_CAMERA_RECORD_TIME)) * 20000; // 60000;

		// Upload Data Setting
		theUploadDataSet = new ArrayList<UploadData>(10);

		// Shake Sensor Setting
		mSensorListener = new ShakeEventListener();
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(mSensorListener,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_UI);

		// shake 센서 감도
		mSensorListener.setMinForce(ShakeEventListener.MIN_FORCEs[Integer.parseInt(theGlobalVar.getSharedPref(GlobalVar.SHARED_CRASH_CRITERIA))]);
		mSensorListener
				.setOnShakeListener(new ShakeEventListener.OnShakeListener() {

					public void onShake() {
						// Auto-generated method stub
						if (recordBtn.isEnabled() == true) {
							return;
						}

						if (getShakeState() == false) {

							setShakeState(true);
							theRestUploadData = NUMBER_UPLOAD_ITEM - 1;

							if (theUploadDataSet.size() > 1) {
								addSendPool(theUploadDataSet
										.get(theUploadDataSet.size() - 2));
							} else {
								// Log.e(GlobalVar.TAG, "아직 업로드 할 내용이 없음");
							}
						}
					}
				});

		// //////////////////////////////////////////////////////////////

		ST.start();

		mapView = (MapView) findViewById(R.id.mapview); // 맵뷰 객체를 가져온다.
		mapView.setBuiltInZoomControls(true); // 줌인,줌아웃 컨트롤을 표시한다.

		mapController = mapView.getController(); // 맵컨트롤러를 가져온다.
		mapController.setZoom(17); // 초기 확대는 17정도로..

		// 위치 매니저를 시스템으로부터 받아온다.
		locM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// 사용가능한 적절한 프로바이더를 받아온다.
		// network (보통 3G망,Wifi AP 위치정보)또는 gps 둘중 하나로 설정된다.
		bestProvider = locM.getBestProvider(new Criteria(), true);

		// 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정한다.
		currentLocation = locM.getLastKnownLocation(bestProvider);

		// 위치 리스너 초기화
		locL = new MyLocationListener();
		// 위치 매니저에 위치 리스너를 셋팅한다.
		// 위치 리스너에서 10000ms (10초) 마다 100미터 이상 이동이 발견되면 업데이트를 하려한다.
//		locM.requestLocationUpdates(bestProvider, 5000, 0, locL);
		locM.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, locL);

		// 처음에 한번 맵뷰에 그려준다.
		updateOverlay(currentLocation);

		Toast.makeText(
				CameraView.this,
				"현재 용량은"
						+ String.valueOf(folderMemoryCheck(MediaPath) + "입니다."),
				Toast.LENGTH_SHORT).show();

		if (folderMemoryCheck(MediaPath) > StorageSize) {
			DeleteTheOldestFile(MediaPath);

		}

		// check external storage//*
		String state = Environment.getExternalStorageState();
		if (!state.equals(Environment.MEDIA_MOUNTED)) {
			Log.d(TAG, "External Storage Media is not mounted.");
		} else {
			EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory()
					.getAbsolutePath();
		}

		// create a SurfaceView instance and add it to the layout
		// SurfaceView surface = new SurfaceView(this);
		SurfaceView surface = (SurfaceView) findViewById(R.id.videoLayout);

		holder = surface.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// 녹화 및 정지 버튼 연결
		recordBtn = (Button) findViewById(R.id.recordBtn);
		recordStopBtn = (Button) findViewById(R.id.recordStopBtn);
		mapHiddenBtn = (Button) findViewById(R.id.mapHiddenBtn);

		final Timer PlayerTimer = new Timer();
		final Timer MapTimer = new Timer();
		final TimerTask Playertask = new TimerTask() {
			public void run() {
				try {

					if (recorder == null)
						return;

					// 녹화 중지 후에 리소스 해제
					recorder.stop();
					recorder.reset();
					chkUploadData();

					recorder.release();
					mCamera.lock();
					recorder = null;

					filename = createFilename();
					startRecording(filename);
					Toast.makeText(CameraView.this, locL.toString(),
							Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		};
		final TimerTask MapTask = new TimerTask() {
			public void run() {
				currentLocation = locM.getLastKnownLocation(bestProvider);
				updateOverlay(currentLocation);
			}
		};

		// MapTimer.schedule(MapTask, 0,10000);
		// 녹화 버튼 리스너
		recordBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				filename = createFilename();
				startRecording(filename);
				PlayerTimer.schedule(Playertask, RecordTime, RecordTime);

				recordBtn.setEnabled(false);
				recordStopBtn.setEnabled(true);

				ObdDataSwitch(true);

			}
		});

		recordStopBtn.setEnabled(false);
		// 녹화 정리 리스너
		recordStopBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				PlayerTimer.cancel();
				stopRecording();
				currentLocation = locM.getLastKnownLocation(bestProvider);
				updateOverlay(currentLocation);
				ST.interrupt();
				MT.interrupt();
				recordBtn.setEnabled(true);
				recordStopBtn.setEnabled(false);

				ObdDataSwitch(false);
			}
		});

		// 맵 보이기 버튼
		mapHiddenBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub

				Button theBtn = (Button) v;
				RelativeLayout.LayoutParams theBtnParams;
				if (theBtn.getText().equals(">")) {
					theBtn.setText("<");
					mapView.setVisibility(View.GONE);

					theBtnParams = new RelativeLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					theBtnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

					theBtn.setLayoutParams(theBtnParams);
				} else {
					theBtn.setText(">");
					mapView.setVisibility(View.VISIBLE);

					theBtnParams = new RelativeLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT,
							ViewGroup.LayoutParams.WRAP_CONTENT);
					theBtnParams.addRule(RelativeLayout.LEFT_OF, R.id.mapview);

					theBtn.setLayoutParams(theBtnParams);
				}
			}
		});

		CameraPreview();

	}

	/*****************************************************************
	 * Accident Upload & ETC
	 *****************************************************************/

	private void chkUploadData() {

		// 업로드 데이터 저장으로 표시
		lastUploadData().setSaved(true);
		try {
			lastUploadData().createDataFile(GlobalVar.DATA_PATH,
					GlobalVar.VIDEO_PATH);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerFactoryConfigurationError e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}

		printUploadData();

		if (theRestUploadData > 0) {
			theRestUploadData--;
			addSendPool(lastUploadData());
		} else if (theRestUploadData == 0) {
			setShakeState(false);
			theRestUploadData = -1;
			// addSendPool(null);
		}

	}

	// Obd Data 받기 시작 정지
	// true : 시작 / false : 정지
	private void ObdDataSwitch(boolean On_Off) {

		if (theGlobalVar.getBlueState() != BluetoothService.STATE_CONNECTED) {
			return;
		}

		Message theMsg;

		if (On_Off) {

			theMsg = theGlobalVar.theBlueCommandHandler.obtainMessage(
					GlobalVar.BLUE_REQ_OBD_INFO, 10, 0);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);
		} else {
			theMsg = theGlobalVar.theBlueCommandHandler
					.obtainMessage(GlobalVar.BLUE_FIN_SEND_DATA);
			theGlobalVar.theBlueCommandHandler.sendMessage(theMsg);

		}

	}

	/** OBD / Geo 정보 추가 핸들러 */
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			UploadData theUploadData;
			switch (msg.what) {
			case GlobalVar.OBD_INFO_FROM_CAMERA:

				// 데이터 들어왔을때

				OBD_Info theInputOBD = (OBD_Info) msg.obj;

				theUploadData = lastUploadData();
				if (theUploadData != null) {
					theUploadData.addObdInfo(theInputOBD);

					// Log.e(GlobalVar.TAG,
					// "Recv OBD : " + theInputOBD.getObdDate());
				} else {
					// Log.e(GlobalVar.TAG, "theUploadData is Null");
				}

				break;
			case GlobalVar.GEO_INFO_FROM_CAMERA:

				GeoInfo theInputGeo = (GeoInfo) msg.obj;

				theUploadData = lastUploadData();
				if (theUploadData != null) {
					theUploadData.addGeoInfo(theInputGeo);

					// Log.e(GlobalVar.TAG,
					// "Recv Geo : " + theInputGeo.getLatitude());
				} else {
					// Log.e(GlobalVar.TAG, "theUploadData is Null");
				}

			}
		}
	};

	private UploadData lastUploadData() {
		if (!theUploadDataSet.isEmpty()) {
			return theUploadDataSet.get(theUploadDataSet.size() - 1);
		} else {
			return null;
		}
	}

	private void printUploadData() {

		for (int i = 0; i < theUploadDataSet.size(); i++) {
			// Log.e(GlobalVar.TAG, "Data " + i + " : "
			// + theUploadDataSet.get(i).printObdData());
		}
	}

	private void addSendPool(UploadData theData) {
		Message theMsg;
		if (theData == null) {
			theGlobalVar.theUploadHandler
					.sendEmptyMessage(GlobalVar.ADD_UPLOAD_DATA);

		} else {
			theMsg = theGlobalVar.theUploadHandler.obtainMessage(
					GlobalVar.ADD_UPLOAD_DATA, theData);
			theGlobalVar.theUploadHandler.sendMessage(theMsg);
		}

	}

	/*****************************************************************
	 * Shake Sensor
	 *****************************************************************/
	private void setShakeState(boolean shake) {
		isShake = shake;

		if (GlobalVar.isDebug) {
			if (shake) {
				Log.e(GlobalVar.TAG, "Shake() ON");
			} else {
				Log.e(GlobalVar.TAG, "Shake() OFF");
				addSendPool(null);

			}
		}

	}

	private synchronized boolean getShakeState() {
		return isShake;
	}

	/*****************************************************************
	 * 안드로이드 기본
	 *****************************************************************/

	public void onBackPressed() {

		onPause();
		super.onBackPressed();

	}

	protected void onPause() {
		if (mCamera != null) {
			mCamera.release();
			mCamera = null;
		}

		if (recorder != null) {
			recorder.release();
			recorder = null;
		}

		if (player != null) {
			player.release();
			player = null;
		}

		super.onPause();
		// Log.e("lifecycle", "onPause()");
	}

	protected void onDestroy() {
		super.onDestroy();
		if (recorder != null) {
			recorder.release();
		}
	}

	/*****************************************************************
	 * Camera
	 *****************************************************************/

	// 녹화 시작 함수
	private void startRecording(String filename) {

		try {
			if (recorder == null) {
				recorder = new MediaRecorder();
			}

			// 기존에 보이던 카메라 뷰 해제

			mCamera.unlock();

			// 객체 정보 설정
			recorder.setCamera(mCamera);
			recorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

			recorder.setProfile(CamcorderProfile.get(CameraQuailty));

			// Log.e(TAG, "current filename : " + filename);

			// 미리보기 디스플레이 surfaceview 화면 설정
			recorder.setOutputFile(filename);
			recorder.setPreviewDisplay(holder.getSurface());
			recorder.prepare();

			recorder.start();

			// 기본 개수 이상 데이터 제거
			if (theUploadDataSet.size() >= MAX_UPLOAD_DATA) {
				theUploadDataSet.remove(0);
			}
			String theDate = filename.substring(
					GlobalVar.VIDEO_PATH.length() + 1, filename.length() - 4);

			// //Log.e(GlobalVar.TAG, "Date : " + theDate);

			UploadData uploadData = new UploadData(theDate);

			theUploadDataSet.add(uploadData);

		} catch (Exception ex) {
			// Log.e(TAG, "Exception : ", ex);

			recorder.release();
			recorder = null;
		}
	}

	private void stopRecording() {
		if (recorder == null)
			return;

		// 녹화 중지 후에 리소스 해제
		recorder.stop();
		recorder.reset();
		recorder.release();
		StopPreview();
		recorder = null;
		CameraPreview();

		chkUploadData();

	}

	private void CameraPreview() {

		mCamera = Camera.open();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException e1) {
			// Auto-generated catch block
			e1.printStackTrace();
		}
		mCamera.startPreview();

	}

	private void StopPreview() {
		mCamera.lock();
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	// 파일 이름 생성하는 함수
	private String createFilename() {
		fileIndex++;

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss",
				Locale.KOREA);
		Date d = new Date();
		String filename = sdf.format(d);

		String newFilename = "";
		if (EXTERNAL_STORAGE_PATH == null || EXTERNAL_STORAGE_PATH.equals("")) {
			// use internal memory
			newFilename = RECORDED_FILE + fileIndex + ".mp4";
		} else {
			newFilename = MediaPath + filename + ".mp4";
		}

		return newFilename;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// Auto-generated method stub
		Log.d(TAG, "Enter surfaceChanged  ---------------------------");
		Camera.Parameters parameters = mCamera.getParameters();
		mCamera.setParameters(parameters);
		mCamera.startPreview();
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// Auto-generated method stub
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;
			// Log.e("created", "asdf");
			// : add more exception handling logic here
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Auto-generated method stub
		// mCamera.stopPreview();
		// mCamera.release();
		// Log.e("destory", "asdf");
		mCamera = null;
	}

	/*****************************************************************
	 * Storage Check
	 *****************************************************************/

	// 저장 폴더의 용량을 확인 하는 함수
	public long folderMemoryCheck(String a_path) {
		long totalMemory = 0;
		File file = new File(a_path);
		File[] childFileList = file.listFiles();

		if (childFileList == null) {
			return 0;
		}

		for (File childFile : childFileList) {
			if (childFile.isDirectory()) {

				totalMemory += folderMemoryCheck(childFile.getAbsolutePath());
			} else {
				totalMemory += childFile.length();
			}
		}
		return totalMemory;
	}

	// 폴더 용량이 일정이상 됬을 경우에 삭제하는 함수
	private void DeleteTheOldestFile(String strFolderPath) {
		long nLastModifiedDate = 0;
		File targetFile = null;
		ArrayList<String> video = new ArrayList<String>();
		File musicfiles = new File(MediaPath);
		File[] arrFiles = new File(strFolderPath).listFiles();

		// 파일리스트에 mp4파일만 읽어서 리스트에 추가시킴
		if (musicfiles.listFiles(new Mp4Filter()).length > 0) {
			for (File file : musicfiles.listFiles(new Mp4Filter())) {
				video.add(file.getName()); // mp4파일을 ArrayList에 추가
			}
		}

		Collections.sort(video);

		if (!video.isEmpty()) {

			// 리스트의 가장 마지막 비디오를 삭제
			targetFile = new File(MediaPath + video.get(0));

			// 타겟파일이 지정 됬을 경우에 파일을 삭제
			if (targetFile != null) {
				targetFile.delete();
				Log.e(GlobalVar.TAG, "파일을 삭제하셨습니다 : " + targetFile.getPath()
						+ "/" + targetFile.getName());
			}
		}
	}

	// 저장용량 체크하는 스레드
	private class StorageThread extends Thread {
		public void run() {
			Thread.currentThread().setName("StroageCheck");
			try {
				while (isStorage) {
					if (folderMemoryCheck(MediaPath) > StorageSize) {
						DeleteTheOldestFile(MediaPath);

					}
					Thread.sleep(10000);
				}
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
				isStorage = false;
			}

			// Log.e("exit", "종료");
		}

		// 녹화버튼이 클릭시에 스레드를 정지시킬 interrupt
		public void interrupt() {
			super.interrupt();
			isStorage = false;
			// Log.e("cancle", "자니?");

		}
	}

	// mp4 파일만 찾는 필터링 클래스
	class Mp4Filter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp4")); // 확장자가 mp4인지 확인
		}

	}

	/*****************************************************************
	 * Google Map
	 *****************************************************************/

	private class MapThread extends Thread {
		public void run() {
			try {
				while (isMap) {
					Thread.currentThread().sleep(MapTime);

					locM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					// 사용가능한 적절한 프로바이더를 받아온다.
					// network (보통 3G망,Wifi AP 위치정보)또는 gps 둘중 하나로 설정된다.
					bestProvider = locM.getBestProvider(new Criteria(), true);

					// 기기에 가지고 있는 마지막 위치정보로 현재위치를 초기 설정한다.
					currentLocation = locM.getLastKnownLocation(bestProvider);

					// 위치 리스너 초기화
					locL = new MyLocationListener();
					// 위치 매니저에 위치 리스너를 셋팅한다.
					// 위치 리스너에서 10000ms (10초) 마다 100미터 이상 이동이 발견되면 업데이트를 하려한다.
					// locM.requestLocationUpdates(bestProvider, 5000, 0, locL);
					// 처음에 한번 맵뷰에 그려준다.
					
					
					updateOverlay(currentLocation);
					SimpleDateFormat theFormat = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss", Locale.KOREA);
					String theDate = theFormat.format(new Date());

					GeoInfo tempGeo = new GeoInfo(
							currentLocation.getLatitude(),
							currentLocation.getLongitude(), theDate);

					Message theMsg = mHandler.obtainMessage(
							GlobalVar.GEO_INFO_FROM_CAMERA, tempGeo);
					mHandler.sendMessage(theMsg);

					Log.e("MapThread", "map updating...........");
				}
			} catch (InterruptedException e) {
				// Auto-generated catch block
				e.printStackTrace();
			}
			updateOverlay(currentLocation);
		}

		public void interrupt() {
			isMap = false;
			Log.e("MapThread", "End.............");
			super.interrupt();

		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		// Auto-generated method stub
		return false;
	}

	public class MyLocationListener implements LocationListener {

		public void onLocationChanged(Location location) {
			// 위치 이동이 발견되었을때 호출될 메소드.
			// 위의 설정에서 10초마다 100미터 이상 이동이 발견되면 호출된다.
			updateOverlay(location);
			Log.e("map", "MapUDATE...........//////");

			// Geo 정보를 핸들러에 전달
			SimpleDateFormat theFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss", Locale.KOREA);
			String theDate = theFormat.format(new Date());

			GeoInfo tempGeo = new GeoInfo(location.getLatitude(),
					location.getLongitude(), theDate);

			Message theMsg = mHandler.obtainMessage(
					GlobalVar.GEO_INFO_FROM_CAMERA, tempGeo);
			mHandler.sendMessage(theMsg);

		}

		public void onProviderDisabled(String provider) {
			Log.d(LOG_TAG, "GPS disabled : " + provider);
		}

		public void onProviderEnabled(String provider) {
			Log.d(LOG_TAG, "GPS Enabled : " + provider);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.d(LOG_TAG, "onStatusChanged : " + provider + " & status = "
					+ status);
		}
	}

	protected void updateOverlay(Location location) {
		// 기존에 화면에 찍어둔 오버레이 (마커들)을 싹 지운다.
		listOfOverlays = mapView.getOverlays(); // 맵뷰에서 오버레이 리스트를 가져온다.
		if (listOfOverlays.size() > 0) {
			listOfOverlays.clear(); // 오버레이가 있을때 싹 지워준다.
			Log.d(LOG_TAG, "clear overlays : " + listOfOverlays.size());
		} else {
			Log.d(LOG_TAG, "empty overlays");
		}

		// Location 객체를 가지고 GeoPoint 객체를 얻어내는 메소드
		GeoPoint geoPoint = getGeoPoint(location);
		// 현재위치를 표시할 이미지
		Drawable marker;

		// 실제 운영소스엔 분기하여 현재위치와 선택위치 이미지를 변경하게 되어있다.
		marker = getResources().getDrawable(R.drawable.ic_launcher);
		marker.setBounds(0, 0, marker.getIntrinsicWidth(),
				marker.getIntrinsicHeight());

		// LocationItemizedOverlay 를 이용하여 현재위치 마커를 찍을 오버레이를 생성한다.
		overlayHere = new LocationItemizedOverlay(marker);
		// touch event 의 null pointer 버그를 방지하기 위해 마커를 찍고 바로 populate 시켜준다.
		overlayHere.mPopulate();
		// 현재위치를 GeoCoder 를 이용하여 대략주소와 위,경도를 Toast 를 통하여 보여준다.
		String geoString = showNowHere(location.getLatitude(),
				location.getLongitude(), true);

		// 현재위치 마커 정의
		OverlayItem overlayItem = new OverlayItem(geoPoint, "here", geoString);
		overlayHere.addOverlay(overlayItem); // 현재위치 오버레이 리스트에 현재위치 마커를 넣는다.

		// 지점정보를 HTTP통신을 통해 서버에서 받아와서 전역변수인 brList (지점리스트)에 넣는다.
		// 성능을 고려하여 쓰레드로 구현이 되어있다.
		// 고다음 지점리스트 오버레이에 넣고 화면에 찍어주는 메소드.
		// showBranchMarker(location.getLatitude(), location.getLongitude(),
		// this.searchType, SEARCH_RANGE);

		// 맵뷰에서 터치이벤트를 받을 오버레이를 추가한다.
		// 특정지점을 오래 눌렀을때 특정 지점 기준으로 재검색을 하기 위하여 터치이벤트를 받아와야한다.
		// mapView.getOverlays().add(new MapTouchDetectorOverlay());

		// 마지막으로 생성된 오버레이레이어를 맵뷰에 추가한다.
		mapView.getOverlays().add(overlayHere);
		mapView.getController().animateTo(geoPoint); // 현재위치로 화면을 이동한다.
		mapView.postInvalidate(); // 맵뷰를 다시 그려준다.
	}

	private GeoPoint getGeoPoint(Location location) {
		if (location == null) {
			return null;
		}
		Double lat = location.getLatitude() * 1E6;
		Double lng = location.getLongitude() * 1E6;
		return new GeoPoint(lat.intValue(), lng.intValue());
	}

	protected class LocationItemizedOverlay extends
			ItemizedOverlay<OverlayItem> {
		private List<OverlayItem> overlays;

		public LocationItemizedOverlay(Drawable defaultMarker) { // 오버레이 생성자
			// 마커 이미지의 가운데 아랫부분이 마커에서 표시하는 포인트가 되게 한다.
			super(boundCenterBottom(defaultMarker));
			overlays = new ArrayList<OverlayItem>();
		}

		@Override
		protected OverlayItem createItem(int i) {
			return overlays.get(i);
		}

		@Override
		public int size() {
			return overlays.size();
		}

		public void addOverlay(OverlayItem overlay) {
			overlays.add(overlay);
			// null pointer 버그때문에 오버레이 아이템 추가후 가능한 빨리 populate 해줘야한다.
			populate();
		}

		public void mPopulate() {
			populate();
		}
	}

	private String showNowHere(double lat, double lng, boolean showOption) {
		StringBuilder geoString = new StringBuilder();
		try {
			Geocoder goecoder = new Geocoder(getApplicationContext(),
					Locale.getDefault());

			Address adr = goecoder.getFromLocation(lat, lng, 1).get(0);

			if (adr.getLocality() != null)
				geoString.append(adr.getLocality()).append(" ");
			if (adr.getThoroughfare() != null)
				geoString.append(adr.getThoroughfare());
			if (!"".equals(geoString.toString()))
				geoString.append("\n\n");
		} catch (Exception e) {
		}
		geoString.append("위도 : ").append(lat).append(" ,경도 : ").append(lng);
		if (showOption) {
			Toast.makeText(getApplicationContext(), geoString.toString(),
					Toast.LENGTH_SHORT).show();
		}
		return geoString.toString();
	}

	private void CheckBluetooth() {
		String theBlueName = theGlobalVar
				.getSharedPref(GlobalVar.SHARED_BLUE_NAME);

		BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();

		if (!mBluetoothAdapter.isEnabled()) {
			GlobalVar.popupToast(CameraView.this, "블루투스가 꺼져있습니다.");

		} else if (theBlueName.length() == 0) {
			GlobalVar.popupToast(CameraView.this, "블루투스 정보를 입력해주세요.");

		} else if (theGlobalVar.getBlueState() != BluetoothService.STATE_CONNECTED) {
			GlobalVar.popupToast(CameraView.this, "OBD Server에 연결되어 있지 않습니다.");

		}

	}

}
