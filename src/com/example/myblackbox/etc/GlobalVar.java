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
	
	public static String EXTRA_DEVICE = "device_info";
	
	public static int RECONNECT_TIME = 5000;
	
	// Web login
	public static final String theURL = "http://192.168.43.226/MyCarServer/";

	public static final int DIALOG_PROGRESS_ID = 1;

	public static final int LOGIN_FLAG_ERROR = 0;
	public static final int LOGIN_FLAG_OK = 1;

	public static final String LOGIN = "login_info";
	

	
	// Bluetooth Command
	public final static int BLUE_SEND_NONE 		= 0;
	public final static int BLUE_REQ_OBD_INFO 	= 1;
	public final static int BLUE_SEND_OBD_INFO 	= 2;
	public final static int BLUE_FIN_SEND_DATA 	= 3;
	public final static int BLUE_CONNECT			= 4;
	public final static int BLUE_DISCONNECT		= 5;
	
	
	
	// Handler
	
	public Handler theBlueCommandHandler;
	public Handler theObdHandler;
	public Handler theCameraHandler;
	
	
	public static final int OBD_INFO_FROM_OBD = 1;
	public static final int OBD_INFO_FROM_CAMERA = 2;

	/** Currnet View */
	private int currentView;
	/** Currnet View */
	public static final int CURRENT_MAIN_VIEW = 0;
	public static final int CURRENT_CAMERA_VIEW = 1;
	public static final int CURRENT_VIDEO_VIEW = 2;
	public static final int CURRENT_OBD_VIEW = 3;
	public static final int CURRENT_SETTING_VIEW = 4;
	
	
	
	
	
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
