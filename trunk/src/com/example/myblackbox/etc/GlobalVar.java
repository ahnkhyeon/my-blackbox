package com.example.myblackbox.etc;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.widget.Toast;

public class GlobalVar extends Application {

	public final static String TAG = "MyBlackBox";
	public static final boolean isDebug = true;

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 2;
	public static final int REQUEST_LOGIN = 3;
	public static final int REQUEST_CAMERA_RESOLUTION = 4;
	public static final int REQUEST_CAMERA_STORAGE = 5;
	public static final int REQUEST_CAMERA_RECORD_TIME = 6;

	public static final String EXTRA_DEVICE = "device_info";
	public static final String CAMERA_RECORD_TIME = "recordTime";
	public static final String CAMERA_STORAGE = "storage";
	public static final String CAMERA_RESOLUTION = "resolution";

	public static int RECONNECT_TIME = 5000;

	// Web login

	public static final int DIALOG_PROGRESS_ID = 1;

	public static final int LOGIN_FLAG_ERROR = 0;
	public static final int LOGIN_FLAG_OK = 1;

	public static final String LOGIN = "login_info";

	// Bluetooth Command
	public final static int BLUE_SEND_NONE = 0;
	public final static int BLUE_REQ_OBD_INFO = 1;
	public final static int BLUE_SEND_OBD_INFO = 2;
	public final static int BLUE_FIN_SEND_DATA = 3;
	public final static int BLUE_CONNECT = 4;
	public final static int BLUE_DISCONNECT = 5;

	// Handler

	public Handler theBlueCommandHandler;
	public Handler theObdHandler;
	public Handler theCameraHandler;
	public Handler theUploadHandler;

	public static final int OBD_INFO_FROM_OBD = 1;
	public static final int OBD_INFO_FROM_CAMERA = 2;
	public static final int GEO_INFO_FROM_CAMERA = 3;
	

	/** Currnet View */
	private int currentView;
	/** Currnet View */
	public static final int CURRENT_MAIN_VIEW = 0;
	public static final int CURRENT_CAMERA_VIEW = 1;
	public static final int CURRENT_VIDEO_VIEW = 2;
	public static final int CURRENT_OBD_VIEW = 3;
	public static final int CURRENT_SETTING_VIEW = 4;

	// Video Storage
	public static final String VIDEO_PATH = "/sdcard/MyBlackBox";
	public static final String DATA_PATH = "/sdcard/MyBlackBox/Data";
	public static final String EVENT_PATH = "/sdcard/MyBlackBox/Event";
	public static final String EVENT_DATA_PATH = "/sdcard/MyBlackBox/Event/Data";

	// Web Address
	public static final String WEB_URL = "http://192.168.200.182/MyCarServer/";

	// Shared Preferences
	public static final String SHARED_BLUE_NAME = "BlueName";
	public static final String SHARED_BLUE_ADDRESS = "BlueAddress";
	public static final String SHARED_LOGIN_ID = "LoginID";
	public static final String SHARED_LOGIN_IDENTITY = "LoginIdentity";
	

	public static final String SHARED_CAMERA_QUAILTY = "CameraQuailty";
	public static final String SHARED_CAMERA_STORAGE_SIZE = "VideoStorageSize";
	public static final String SHARED_CAMERA_RECORD_TIME = "RecordSize";
	
	// Upload Data
	public static final int ADD_UPLOAD_DATA = 1;
	
	
	// Bluetooth Status
	private int BLUE_TOOTH_STATE;
	
	public void setBlueState(int state) {
		BLUE_TOOTH_STATE = state;
	}
	
	public synchronized int getBlueState() {
		return BLUE_TOOTH_STATE;
	}


	public static void popupToast(Context theCntext, String inString) {
		Toast.makeText(theCntext, inString, Toast.LENGTH_SHORT).show();

	}

	public final String getSharedPref(String getName) {

		SharedPreferences thePrefs = getSharedPreferences("settingValues",
				MODE_PRIVATE);

		return thePrefs.getString(getName, "");
	}

	public int getCurrentView() {
		return currentView;
	}

	public void setCurrentView(int currentView) {
		this.currentView = currentView;
	}
}
